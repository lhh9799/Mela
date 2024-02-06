package com.ssafy.api.teamspace.service;

import com.ssafy.api.file.service.FileService;
import com.ssafy.api.teamspace.request.ScheduleRegisterPostReq;
import com.ssafy.api.teamspace.request.ScheduleUpdatePutReq;
import com.ssafy.api.teamspace.request.TeamspaceRegisterPostReq;
import com.ssafy.api.teamspace.request.TeamspaceUpdatePutReq;
import com.ssafy.api.teamspace.response.TeamspaceListRes;
import com.ssafy.api.teamspace.response.TeamspaceMemberListRes;
import com.ssafy.api.teamspace.response.TeamspaceRes;
import com.ssafy.common.util.NotificationUtil;
import com.ssafy.db.entity.Schedule;
import com.ssafy.db.entity.Teamspace;
import com.ssafy.db.entity.TeamspaceMember;
import com.ssafy.db.entity.User;
import com.ssafy.db.repository.*;
import lombok.extern.java.Log;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

import static com.ssafy.common.util.ExtensionUtil.isValidImageExtension;

@Transactional
@Service("teamspaceService")
public class TeamspaceServiceImpl implements TeamspaceService{

    @Autowired
    TeamspaceRepository teamspaceRepository;
    @Autowired
    TeamspaceRepositorySupport teamspaceRepositorySupport;

    @Autowired
    TeamspaceMemberRepository teamspaceMemberRepository;
    @Autowired
    TeamspaceMemberRepositorySupport teamspaceMemberRepositorySupport;

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserRepositorySupport userRepositorySupport;

    @Autowired
    FileRepository fileRepository;
    @Autowired
    FileRepositorySupport fileRepositorySupport;

    @Autowired
    ScheduleRepository scheduleRepository;
    @Autowired
    ScheduleRepositorySupport scheduleRepositorySupport;

    @Autowired
    NotificationUtil notificationUtil;

    @Autowired
    FileService fileService;

    //지원하는 동영상 확장자 배열
    private String[] supportedImageExtensions = {"png", "jpg", "jpeg"};

    @Override
    public Teamspace createTeamspace(TeamspaceRegisterPostReq registerInfo, Long userIdx, MultipartFile teamspacePicture, MultipartFile teamspaceBackgroundPicture) {
        // 팀 스페이스 썸네일 저장
        // 팀 스페이스 썸네일 사진 파일 idx 얻기
        // ...
        System.out.println("파일: " +  registerInfo.getTeamspace_picture_file_idx());

        com.ssafy.db.entity.File teamspacePictureRecord = null;
        com.ssafy.db.entity.File teamspaceBackgroundPictureRecord = null;

        //팀 스페이스 메인 이미지 저장
        if(teamspacePicture != null) {
            //클라이언트가 업로드한 파일의 확장자 추출
            String extension = FilenameUtils.getExtension(teamspacePicture.getOriginalFilename());

            if(isValidImageExtension(extension)) {
                teamspacePictureRecord = fileService.saveFile(teamspacePicture, "");
                teamspacePictureRecord = fileService.addTableRecord(teamspacePictureRecord);
            }
        }

        //팀 스페이스 배경 이미지 저장
        if(teamspaceBackgroundPicture != null) {
            //클라이언트가 업로드한 파일의 확장자 추출
            String extension = FilenameUtils.getExtension(teamspaceBackgroundPicture.getOriginalFilename());

            if(isValidImageExtension(extension)) {
                teamspaceBackgroundPictureRecord = fileService.saveFile(teamspaceBackgroundPicture, "");
                teamspaceBackgroundPictureRecord = fileService.addTableRecord(teamspaceBackgroundPictureRecord);
            }
        }

        // 팀 스페이스 생성
        Teamspace teamspace = new Teamspace();
        teamspace.setTeamName(registerInfo.getTeamName());
        teamspace.setStartDate(registerInfo.getStartDate());
        teamspace.setEndDate(registerInfo.getEndDate());
        teamspace.setHost(userRepository.getOne(userIdx));
        teamspace.setTeamDescription(registerInfo.getTeamDescription());
        registerInfo.setTeamspace_picture_file_idx(teamspacePictureRecord);
        teamspace.setTeamspacePictureFileIdx(registerInfo.getTeamspace_picture_file_idx());
        teamspace.setTeamspaceBackgroundPictureFileIdx(teamspaceBackgroundPictureRecord);
        teamspaceRepository.save(teamspace);

        // host 팀 멤버로 추가
        addMember(teamspace.getTeamspaceIdx(), userIdx);

        return teamspaceRepository.save(teamspace);

    }

    @Override
    public Teamspace getTeamspaceById(Long id) {
        Teamspace teamspace = teamspaceRepository.getOne(id);

        return teamspace;
    }

    @Override
    public Teamspace updateTeamspace(Teamspace teamspace, TeamspaceUpdatePutReq updateInfo) {
        // 파일 삭제
        if (teamspace.getTeamspacePictureFileIdx() != null) {
            fileRepository.delete(teamspace.getTeamspacePictureFileIdx());
        }
        if(teamspace.getTeamspaceBackgroundPictureFileIdx() != null) {
            fileRepository.delete(teamspace.getTeamspaceBackgroundPictureFileIdx());
        }

        // 사진 파일 저장
        // ...
        // 사진 파일 idx 얻어서 그 값으로 변경해줘야 함

        // 팀스페이스 정보 수정
        teamspace.setTeamName(updateInfo.getTeamName());
        teamspace.setStartDate(updateInfo.getStartDate());
        teamspace.setEndDate(updateInfo.getEndDate());
        teamspace.setTeamDescription(updateInfo.getTeamDescription());
        teamspace.setTeamspacePictureFileIdx(updateInfo.getTeamspace_picture_file_idx());
        teamspace.setTeamspaceBackgroundPictureFileIdx(updateInfo.getTeamspace_background_picture_file_idx());

        return teamspaceRepository.save(teamspace);
    }



    @Override
    public void deleteTeamspace(Teamspace teamspace) {
        teamspaceRepository.delete(teamspace);
    }

    @Override
    public List<Teamspace> getTeamspaceList(Long userIdx) {
        List<Teamspace> teamspaces = teamspaceRepositorySupport.findTeamspaceList(userRepository.getOne(userIdx));

        System.out.println("teaspacelist " + teamspaces);

        return teamspaces;
    }

    @Override
    public void addMember(Long teamspaceIdx, Long userIdx) {
        Optional<TeamspaceMember> teamspaceMemberTmp
                = teamspaceMemberRepository.findByUserIdxAndTeamspaceIdx(userRepository.getOne(userIdx), teamspaceRepository.getOne(teamspaceIdx));

        if(teamspaceMemberTmp.isPresent()) {
            return;
        }

        TeamspaceMember teamspaceMember = new TeamspaceMember();
        teamspaceMember.setTeamspaceIdx(teamspaceRepository.getOne(teamspaceIdx));
        teamspaceMember.setUserIdx(userRepository.getOne(userIdx));
        teamspaceMemberRepository.save(teamspaceMember);

        if (teamspaceMember.getUserIdx().getUserIdx() != teamspaceRepository.getOne(teamspaceIdx).getHost().getUserIdx()) {

            String message = teamspaceRepository.getOne(teamspaceIdx).getTeamName();
            if(message.length() >= 7) {
                message = message.substring(0, 7) + "..";
            }

            message = "'" + message + "' 팀스페이스에 초대되었습니다.";

            notificationUtil.sendNotification(message, userRepository.getOne(userIdx));
        }
    }

    @Override
    public void leaveTeamspace(Long teamspaceIdx, Long userIdx) {
        Teamspace teamspace = teamspaceRepository.getOne(teamspaceIdx);
        User user = userRepository.getOne(userIdx);
        Optional<TeamspaceMember> teamspaceMember =  teamspaceMemberRepository.findByUserIdxAndTeamspaceIdx(user, teamspace);
        if(teamspaceMember.isPresent()) {
            teamspaceMemberRepository.delete(teamspaceMember.get());
        }
    }

    @Override
    public List<TeamspaceMemberListRes> getTeamspaceMemberList(Long teamspaceIdx) {
        Teamspace teamspace = teamspaceRepository.getOne(teamspaceIdx);
        List<TeamspaceMemberListRes> users = teamspaceRepositorySupport.findTeamspaceMemberList(teamspace);
        return users;
    }

    @Override
    public void addSchedule(ScheduleRegisterPostReq registInfo, Long teamspaceIdx) throws Exception {
        Schedule schedule = new Schedule();
        schedule.setTeamspaceIdx(teamspaceRepository.getOne(teamspaceIdx)); // EntityNotFoundException
        schedule.setContent(registInfo.getContent());
        schedule.setPlace(registInfo.getPlace());
        schedule.setStartTime(registInfo.getStartTime());
        schedule.setEndTime(registInfo.getEndTime());
        scheduleRepository.save(schedule);

        List<TeamspaceMemberListRes> lists = getTeamspaceMemberList(teamspaceIdx);
        for (TeamspaceMemberListRes list : lists) {
            User user = userRepository.getOne(list.getUserIdx());
            // TODO:

            String message = schedule.getContent();
            if (message.length() >= 7) {
                message = message.substring(0, 7) + "..";
            }
            message = "팀스페이스에 '" + message + "' 일정이 추가되었습니다.";
            notificationUtil.sendNotification(message, user);
        }
    }

    @Override
    public void deleteSchedule(Long scheduleId) {
        Schedule schedule = scheduleRepository.getOne(scheduleId);
        scheduleRepository.delete(schedule);
    }

    @Override
    public void updateSchedule(ScheduleUpdatePutReq updateInfo, Long scheduleIdx) throws EntityNotFoundException {
        Schedule schedule = scheduleRepository.getOne(scheduleIdx);
        schedule.setContent(updateInfo.getContent());
        schedule.setPlace(updateInfo.getPlace());
        schedule.setStartTime(updateInfo.getStartTime());
        schedule.setEndTime(updateInfo.getEndTime());
        scheduleRepository.save(schedule);
    }

    @Override
    public List<Schedule> getScheduleList(Long teamspaceIdx) {
        List<Schedule> schedules = scheduleRepository.findByTeamspaceIdx(teamspaceRepository.getOne(teamspaceIdx));

        return schedules;
    }


}
