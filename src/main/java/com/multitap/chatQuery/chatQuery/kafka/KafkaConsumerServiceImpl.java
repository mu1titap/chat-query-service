package com.multitap.chatQuery.chatQuery.kafka;

import com.multitap.chatQuery.chatQuery.dto.in.ChatRequestDto;
import com.multitap.chatQuery.chatQuery.dto.in.MemberRequestDto;
import com.multitap.chatQuery.chatQuery.dto.in.MentoringRequestDto;
import com.multitap.chatQuery.chatQuery.entity.ChatList;
import com.multitap.chatQuery.chatQuery.feignClient.MentoringServiceFeignClient;
import com.multitap.chatQuery.chatQuery.feignClient.dto.SessionRoomResponseDto;
import com.multitap.chatQuery.chatQuery.infrastructure.ChatListRepository;
import com.multitap.chatQuery.common.exception.BaseException;
import com.multitap.chatQuery.common.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Criteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerServiceImpl implements KafkaConsumerService {

    private final ChatListRepository chatListRepository;
    private final MentoringServiceFeignClient mentoringServiceFeignClient;
    private final MongoTemplate mongoTemplate;

    @Override
    public void addChat(ChatRequestDto chatRequestDto, String mentoringSessionUuid) {

        // 1. mentoringSessionUuid로 기존 데이터 조회
        Optional<ChatList> existingChat = chatListRepository.findById(mentoringSessionUuid);

//        SessionRoomResponseDto sessionRoom = mentoringServiceFeignClient.getSessionRoom(mentoringSessionUuid);
//        MentoringRequestDto mentoringRequestDto = MentoringRequestDto.from(sessionRoom);

        // 2. 새 엔티티 생성 (기존 데이터가 있으면 ID 유지)
        ChatList chatList = existingChat
                .map(chat -> ChatList.builder()
                        .id(chat.getId()) // 기존 ID 유지
                        .chatInfo(chatRequestDto) // 새로운 ChatInfo로 대체
                        .mentoringInfo(chat.getMentoringInfo()) // 필요시 기존 데이터 유지
                        .build())
                .orElseGet(() -> {
                    log.info("Creating new chat list {}", mentoringSessionUuid);
                    MentoringRequestDto mentoringRequestDto = MentoringRequestDto.from(mentoringServiceFeignClient.findSessionRoomBySessionUuid(mentoringSessionUuid));
                    log.info("{}", mentoringRequestDto);
                    return chatRequestDto.toEntity(chatRequestDto, mentoringRequestDto, mentoringSessionUuid); // 없으면 새로 생성
                });

        // 3. 저장 (기존 ID면 업데이트, 없으면 새로 저장)
        chatListRepository.save(chatList);
        log.info("{} 채팅 성공: {}", existingChat.isPresent() ? "채팅 업데이트" : "채팅 저장", chatRequestDto.getMessage());
    }

    @Override
    public void addMemberList(MemberRequestDto memberRequestDto, String mentoringSessionUuid) {

        Query query = new Query(Criteria.where("_id").is(mentoringSessionUuid));

        Update update = new Update()
                .addToSet("memberInfo", memberRequestDto)
                .setOnInsert("mentoringInfo", MentoringRequestDto.from(
                        mentoringServiceFeignClient.findSessionRoomBySessionUuid(mentoringSessionUuid)));

        mongoTemplate.upsert(query, update, ChatList.class);
    }
}
