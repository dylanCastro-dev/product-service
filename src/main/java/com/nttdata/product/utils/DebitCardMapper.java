package com.nttdata.product.utils;

import com.nttdata.product.model.DebitCard;
import org.openapitools.model.CardResponse;
import org.openapitools.model.CardBody;
import org.openapitools.model.CardTemplateResponse;

import java.util.List;
import java.util.stream.Collectors;

public class DebitCardMapper {

    public static CardResponse toCardResponse(DebitCard card) {
        return new CardResponse()
                .id(card.getId())
                .cardNumber(card.getCardNumber())
                .customerId(card.getCustomerId())
                .primaryAccountId(card.getPrimaryAccountId())
                .active(card.isActive())
                .linkedAccountIds(card.getLinkedAccountIds());
    }

    public static DebitCard toDebitCard(CardBody card) {
        return DebitCard.builder()
                .cardNumber(card.getCardNumber())
                .customerId(card.getCustomerId())
                .primaryAccountId(card.getPrimaryAccountId())
                .active(card.getActive())
                .linkedAccountIds(card.getLinkedAccountIds())
                .build();
    }

    public static CardTemplateResponse toResponse(DebitCard card, int status, String message) {
        return new CardTemplateResponse()
                .status(status)
                .message(message)
                .addCardsItem(toCardResponse(card));
    }

    public static CardTemplateResponse toResponse(List<DebitCard> lstCards, int status, String message) {
        List<CardResponse> cards = lstCards.stream()
                .map(DebitCardMapper::toCardResponse)
                .collect(Collectors.toList());

        return new CardTemplateResponse()
                .status(status)
                .message(message)
                .cards(cards);
    }

}
