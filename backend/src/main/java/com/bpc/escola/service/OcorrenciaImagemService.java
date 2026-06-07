package com.bpc.escola.service;

import com.bpc.escola.domain.Ocorrencia;
import com.bpc.escola.domain.OcorrenciaImagem;
import com.bpc.escola.dto.OcorrenciaImagemDTO;
import com.bpc.escola.exception.BusinessException;
import com.bpc.escola.repository.OcorrenciaImagemRepository;
import com.bpc.escola.repository.OcorrenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OcorrenciaImagemService {

    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final int MAX_IMAGENS_POR_OCORRENCIA = 10;
    private static final long MAX_TAMANHO_BYTES = 5L * 1024 * 1024;

    private final OcorrenciaImagemRepository imagemRepository;
    private final OcorrenciaRepository ocorrenciaRepository;

    @Value("${app.uploads.dir:uploads}")
    private String uploadsDir;

    public List<OcorrenciaImagemDTO> listarPorOcorrencia(Long ocorrenciaId) {
        return imagemRepository.findByOcorrenciaIdOrderByCriadoEmAsc(ocorrenciaId).stream()
                .map(OcorrenciaImagemDTO::from)
                .toList();
    }

    @Transactional
    public List<OcorrenciaImagemDTO> salvarImagens(Long ocorrenciaId, List<MultipartFile> arquivos) {
        if (arquivos == null || arquivos.isEmpty()) {
            return List.of();
        }
        Ocorrencia ocorrencia = ocorrenciaRepository.findById(ocorrenciaId)
                .orElseThrow(() -> new BusinessException("Ocorrência não encontrada.", "OCORRENCIA_NAO_ENCONTRADA"));

        long existentes = imagemRepository.countByOcorrenciaId(ocorrenciaId);
        if (existentes + arquivos.size() > MAX_IMAGENS_POR_OCORRENCIA) {
            throw new BusinessException(
                    "Máximo de " + MAX_IMAGENS_POR_OCORRENCIA + " imagens por ocorrência.",
                    "LIMITE_IMAGENS_EXCEDIDO");
        }

        Path diretorio = diretorioOcorrencia(ocorrenciaId);
        try {
            Files.createDirectories(diretorio);
        } catch (IOException e) {
            throw new BusinessException("Não foi possível preparar o armazenamento.", "UPLOAD_ERRO", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return arquivos.stream()
                .filter(file -> file != null && !file.isEmpty())
                .map(file -> salvarArquivo(ocorrencia, diretorio, file))
                .map(OcorrenciaImagemDTO::from)
                .toList();
    }

    public Resource carregarArquivo(Long imagemId) {
        OcorrenciaImagem imagem = imagemRepository.findById(imagemId)
                .orElseThrow(() -> new BusinessException("Imagem não encontrada.", "IMAGEM_NAO_ENCONTRADA", HttpStatus.NOT_FOUND));
        Path arquivo = diretorioOcorrencia(imagem.getOcorrencia().getId()).resolve(imagem.getNomeArmazenado());
        if (!Files.exists(arquivo)) {
            throw new BusinessException("Arquivo não encontrado.", "ARQUIVO_NAO_ENCONTRADO", HttpStatus.NOT_FOUND);
        }
        try {
            Resource resource = new UrlResource(arquivo.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BusinessException("Arquivo não encontrado.", "ARQUIVO_NAO_ENCONTRADO", HttpStatus.NOT_FOUND);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new BusinessException("Arquivo inválido.", "ARQUIVO_INVALIDO", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String contentType(Long imagemId) {
        return imagemRepository.findById(imagemId)
                .map(OcorrenciaImagem::getContentType)
                .orElse("application/octet-stream");
    }

    private OcorrenciaImagem salvarArquivo(Ocorrencia ocorrencia, Path diretorio, MultipartFile file) {
        validarArquivo(file);
        String extensao = extensaoDe(file);
        String nomeArmazenado = UUID.randomUUID() + extensao;
        Path destino = diretorio.resolve(nomeArmazenado);
        try {
            Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException("Falha ao salvar imagem.", "UPLOAD_ERRO", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        OcorrenciaImagem imagem = OcorrenciaImagem.builder()
                .ocorrencia(ocorrencia)
                .nomeOriginal(sanitizarNome(file.getOriginalFilename()))
                .nomeArmazenado(nomeArmazenado)
                .contentType(file.getContentType())
                .tamanhoBytes(file.getSize())
                .criadoEm(RelogioSaoPaulo.dataHora())
                .build();
        return imagemRepository.save(imagem);
    }

    private void validarArquivo(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !TIPOS_PERMITIDOS.contains(contentType)) {
            throw new BusinessException(
                    "Formato não suportado. Use JPEG, PNG, WebP ou GIF.",
                    "TIPO_ARQUIVO_INVALIDO");
        }
        if (file.getSize() > MAX_TAMANHO_BYTES) {
            throw new BusinessException("Cada imagem deve ter no máximo 5 MB.", "ARQUIVO_MUITO_GRANDE");
        }
    }

    private Path diretorioOcorrencia(Long ocorrenciaId) {
        return Path.of(uploadsDir, "ocorrencias", String.valueOf(ocorrenciaId));
    }

    private String extensaoDe(MultipartFile file) {
        return switch (file.getContentType()) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> "";
        };
    }

    private String sanitizarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            return "imagem";
        }
        return nome.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
