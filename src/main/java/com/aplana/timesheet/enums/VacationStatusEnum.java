package com.aplana.timesheet.enums;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public enum VacationStatusEnum implements TSEnum {
    APPROVEMENT_WITH_PM(57, "На согласовании РПГ"),
    APPROVED_BY_PM(58, "Согласовано РПГ"),
    APPROVEMENT_WITH_LM(59, "На согласовании ЛР"),
    APPROVED(60, "Утверждено"),
    REJECTED(61, "Отклонено"),
    CREATED(133, "Создано");

    private final int id;
    private final String name;

    private VacationStatusEnum(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * возвращает список статусов для неутвержденных отпусков
     */
    public static List<Integer> getNotApprovedStatuses() {
        return Arrays.asList(APPROVED_BY_PM.getId(), APPROVEMENT_WITH_LM.getId(), APPROVEMENT_WITH_PM.getId());
    }

    public static List<VacationStatusEnum> getNotApprovedStatusesEnum() {
        return Arrays.asList(APPROVED_BY_PM, APPROVEMENT_WITH_LM, APPROVEMENT_WITH_PM);
    }

    public static VacationStatusEnum getById( final Integer id ) {
        if (id == null) {return null;}
        return Iterables.tryFind(Arrays.asList(VacationStatusEnum.values()), new Predicate<VacationStatusEnum>() {
            @Override
            public boolean apply(@Nullable VacationStatusEnum input) {
                return input.getId() == id;
            }
        }).orNull();
    }
}
