package com.gasen.usercenterbackend.common;

public enum ItemsEunm {

    ITEM_1(1, 88),
    ITEM_2(2, 88),
    ITEM_3(3, 88),
    ITEM_4(4, 88);

    ItemsEunm(int itemId, int price) {
        this.itemId = itemId;
        this.price = price;
    }

    private final int itemId;

    private final int price;

    public static int getPrice(int itemId) {
        for (ItemsEunm item : ItemsEunm.values()) {
            if (item.itemId == itemId) {
                return item.price;
            }
        }
        return -1;
    }

}
