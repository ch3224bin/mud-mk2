package com.jefflife.mudmk2.gameplay.application.port.out;

import com.jefflife.mudmk2.gameplay.application.service.model.template.RoomInfoVariables;

public interface SendRoomInfoMessagePort {
    void sendMessage(RoomInfoVariables roomInfoVariables);
}
