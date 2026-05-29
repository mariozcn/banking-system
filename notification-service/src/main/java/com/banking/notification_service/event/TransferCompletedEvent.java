package com.banking.notification_service.event;

import java.math.BigDecimal;

public record TransferCompletedEvent(
        String sender,
        String receiver,
        String senderName,
        String receiverName,
        String currency,
        BigDecimal amount
) {
}
