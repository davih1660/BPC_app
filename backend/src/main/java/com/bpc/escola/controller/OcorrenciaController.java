package com.bpc.escola.controller;

import com.bpc.escola.domain.enums.StatusOcorrencia;
import com.bpc.escola.dto.OcorrenciaDTO;
import com.bpc.escola.dto.OcorrenciaImagemDTO;
import com.bpc.escola.service.OcorrenciaImagemService;
import com.bpc.escola.service.OcorrenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ocorrencias")
@RequiredArgsConstructor
public class OcorrenciaController {

    private final OcorrenciaService ocorrenciaService;
    private final OcorrenciaImagemService imagemService;

    @GetMapping
    public List<OcorrenciaDTO> listar(@RequestParam(required = false) StatusOcorrencia status) {
        return ocorrenciaService.listar(status);
    }

    @PostMapping
    public OcorrenciaDTO criar(
            @RequestBody OcorrenciaDTO dto,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioId) {
        Long uid = usuarioId != null ? usuarioId : dto.usuarioId();
        return ocorrenciaService.criar(dto, uid);
    }

    @PatchMapping("/{id}/status")
    public OcorrenciaDTO atualizarStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ocorrenciaService.atualizarStatus(id, StatusOcorrencia.valueOf(body.get("status")));
    }

    @PostMapping(value = "/{id}/imagens", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<OcorrenciaImagemDTO> uploadImagens(
            @PathVariable Long id,
            @RequestParam("arquivos") List<MultipartFile> arquivos) {
        return imagemService.salvarImagens(id, arquivos);
    }

    @GetMapping("/imagens/{imagemId}/arquivo")
    public ResponseEntity<Resource> baixarImagem(@PathVariable Long imagemId) {
        Resource resource = imagemService.carregarArquivo(imagemId);
        String contentType = imagemService.contentType(imagemId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(resource);
    }
}
