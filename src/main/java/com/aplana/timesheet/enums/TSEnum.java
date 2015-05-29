package com.aplana.timesheet.enums;

/**
 * @author eshangareev
 * @version 1.0
 */
public interface TSEnum {
    int getId();

    String getName();

    //TODO iziyangirov может стоит добавить сюда getById? или добавить абстрактный класс (например AbctractTSEnum),
    // который бы реализовывал эту функциональность для всех энумов
}
