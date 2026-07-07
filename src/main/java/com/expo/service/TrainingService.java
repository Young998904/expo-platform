package com.expo.service;

import com.expo.domain.AdminUser;
import com.expo.domain.Training;
import com.expo.domain.TrainingAssignment;
import com.expo.domain.TrainingStatus;
import com.expo.repository.AdminUserRepository;
import com.expo.repository.TrainingAssignmentRepository;
import com.expo.repository.TrainingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 교육 생성·배정·수강 진행률 관리.
 * 이수 기준 = 시청 최대 위치가 영상 길이의 95% 이상(영상 끝) 도달.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TrainingService {

    // 이수 임계(영상 길이의 95%)
    private static final double COMPLETION_THRESHOLD = 95.0;
    private static final Pattern VIDEO_ID = Pattern.compile("(?:v=|youtu\\.be/|embed/)([A-Za-z0-9_-]{11})");

    private final TrainingRepository trainingRepository;
    private final TrainingAssignmentRepository assignmentRepository;
    private final AdminUserRepository adminUserRepository;

    @Transactional(readOnly = true)
    public List<Training> listTrainings() {
        return trainingRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Training getTraining(Long id) {
        return trainingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("교육을 찾을 수 없습니다."));
    }

    public Long create(String title, String description, String youtubeUrl, String creatorUsername) {
        Training t = new Training();
        t.setTitle(title);
        t.setDescription(description);
        t.setYoutubeUrl(youtubeUrl);
        t.setYoutubeVideoId(parseVideoId(youtubeUrl));
        t.setCreatedBy(adminUserRepository.findByUsername(creatorUsername).orElse(null));
        return trainingRepository.save(t).getId();
    }

    public void assign(Long trainingId, Long assigneeId) {
        if (assignmentRepository.existsByTrainingIdAndAssigneeId(trainingId, assigneeId)) {
            throw new IllegalStateException("이미 배정된 대상입니다.");
        }
        Training training = getTraining(trainingId);
        AdminUser assignee = adminUserRepository.findById(assigneeId)
                .orElseThrow(() -> new IllegalArgumentException("배정 대상을 찾을 수 없습니다."));
        TrainingAssignment a = new TrainingAssignment();
        a.setTraining(training);
        a.setAssignee(assignee);
        assignmentRepository.save(a);
    }

    /** 교육별 대상자 수강율(전체 관리자 조회). */
    @Transactional(readOnly = true)
    public List<TrainingAssignment> assignmentsForTraining(Long trainingId) {
        return assignmentRepository.findByTrainingWithAssignee(trainingId);
    }

    /** 박람회 관리자의 배정 교육 목록. */
    @Transactional(readOnly = true)
    public List<TrainingAssignment> assignmentsForAssignee(Long assigneeId) {
        return assignmentRepository.findByAssigneeWithTraining(assigneeId);
    }

    @Transactional(readOnly = true)
    public TrainingAssignment getAssignmentForWatch(Long id) {
        return assignmentRepository.findByIdWithTraining(id)
                .orElseThrow(() -> new IllegalArgumentException("배정 교육을 찾을 수 없습니다."));
    }

    /**
     * 수강 진행률 갱신. 되감기·시킹은 무시하고 최대 도달 위치가 늘어날 때만 반영한다.
     * 영상 길이의 95% 이상 도달 시 이수 완료 처리.
     */
    public TrainingAssignment updateProgress(Long assignmentId, int positionSec, int durationSec) {
        TrainingAssignment a = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("배정 교육을 찾을 수 없습니다."));
        if (durationSec > 0) {
            a.setDurationSec(durationSec);
        }
        if (positionSec > a.getLastPositionSec()) {
            a.setLastPositionSec(positionSec);
        }
        if (a.getDurationSec() > 0) {
            a.setProgressRate(Math.min(100.0, a.getLastPositionSec() * 100.0 / a.getDurationSec()));
        }
        if (a.getStatus() == TrainingStatus.NOT_STARTED) {
            a.setStatus(TrainingStatus.IN_PROGRESS);
        }
        if (a.getProgressRate() >= COMPLETION_THRESHOLD && a.getStatus() != TrainingStatus.COMPLETED) {
            a.setStatus(TrainingStatus.COMPLETED);
            a.setCompletedAt(LocalDateTime.now());
        }
        return a;
    }

    private String parseVideoId(String url) {
        if (url != null) {
            Matcher m = VIDEO_ID.matcher(url);
            if (m.find()) {
                return m.group(1);
            }
            // 순수 11자리 ID를 직접 붙여넣은 경우
            if (url.matches("[A-Za-z0-9_-]{11}")) {
                return url;
            }
        }
        throw new IllegalArgumentException("유효한 YouTube 링크가 아닙니다.");
    }
}
