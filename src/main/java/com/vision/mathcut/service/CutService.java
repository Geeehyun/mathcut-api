package com.vision.mathcut.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vision.mathcut.domain.Cut;
import com.vision.mathcut.domain.User;
import com.vision.mathcut.dto.cut.CutDetail;
import com.vision.mathcut.dto.cut.CutListItem;
import com.vision.mathcut.dto.cut.CutSaveRequest;
import com.vision.mathcut.repository.CutRepository;
import com.vision.mathcut.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CutService {

    private final CutRepository cutRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<CutListItem> getMyCuts(Long userId) {
        return cutRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(CutListItem::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public CutDetail getCut(Long cutId, Long userId) {
        Cut cut = findCutAndCheckOwner(cutId, userId);
        try {
            JsonNode canvasData = objectMapper.readTree(cut.getCanvasData());
            return new CutDetail(cut.getId(), cut.getTitle(), canvasData, cut.getThumbnail(),
                    cut.getCreatedAt(), cut.getUpdatedAt());
        } catch (Exception e) {
            throw new RuntimeException("캔버스 데이터 파싱 오류");
        }
    }

    @Transactional
    public Long saveCut(CutSaveRequest request, Long userId) {
        User user = userRepository.getReferenceById(userId);
        Cut cut = Cut.builder()
                .user(user)
                .title(request.getTitle())
                .canvasData(request.getCanvasData().toString())
                .thumbnail(request.getThumbnail())
                .createdBy(userId)
                .build();
        return cutRepository.save(cut).getId();
    }

    @Transactional
    public Long updateCut(Long cutId, CutSaveRequest request, Long userId) {
        Cut cut = findCutAndCheckOwner(cutId, userId);
        cut.update(request.getTitle(), request.getCanvasData().toString(), request.getThumbnail(), userId);
        return cut.getId();
    }

    @Transactional
    public void deleteCut(Long cutId, Long userId) {
        Cut cut = findCutAndCheckOwner(cutId, userId);
        cutRepository.delete(cut);
    }

    private Cut findCutAndCheckOwner(Long cutId, Long userId) {
        Cut cut = cutRepository.findById(cutId)
                .orElseThrow(() -> new NoSuchElementException("컷을 찾을 수 없습니다."));
        if (!cut.getUser().getId().equals(userId)) {
            throw new SecurityException("접근 권한이 없습니다.");
        }
        return cut;
    }
}
