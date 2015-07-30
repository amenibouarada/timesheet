package com.aplana.timesheet.service;

import argo.jdom.JsonArrayNodeBuilder;
import com.aplana.timesheet.dao.DictionaryItemDAO;
import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.enums.DictionaryEnum;
import com.aplana.timesheet.enums.TSEnum;
import com.aplana.timesheet.enums.TypesOfActivityEnum;
import com.aplana.timesheet.enums.UndertimeCausesEnum;
import com.aplana.timesheet.util.JsonUtil;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static argo.jdom.JsonNodeBuilders.*;

@Service
public class DictionaryItemService {

    @Autowired
    private DictionaryItemDAO dictionaryItemDAO;

    @Transactional(readOnly = true)
    public List<DictionaryItem> getCategoryOfActivity() {
        return dictionaryItemDAO.getItemsByDictionaryId(DictionaryEnum.CATEGORY_OF_ACTIVITY.getId());
    }

    @Transactional(readOnly = true)
    public List<DictionaryItem> getTypesOfActivity() {
        List<DictionaryItem> actTypes = dictionaryItemDAO.getItemsByDictionaryIdAndOrderById(DictionaryEnum.TYPES_OF_ACTIVITY.getId());

        Ordering<DictionaryItem> byOrder = new Ordering<DictionaryItem>() {
            @Override
            public int compare(DictionaryItem item1, DictionaryItem item2) {
                return Ints.compare(TypesOfActivityEnum.getById(item1.getId()).getOrder(), TypesOfActivityEnum.getById(item2.getId()).getOrder());
            }
        };

        Collections.sort(actTypes, byOrder);

        return Lists.newArrayList(Iterables.filter(actTypes, new Predicate<DictionaryItem>() {      //убираем из результатов отпуск
            @Override
            public boolean apply(@Nullable DictionaryItem input) {
                return !input.getId().equals(TypesOfActivityEnum.VACATION.getId()) &&
                        !input.getId().equals(TypesOfActivityEnum.ILLNESS.getId()) &&
                        !input.getId().equals(TypesOfActivityEnum.COMPENSATORY_HOLIDAY.getId());
            }
        }));
    }

    @Transactional(readOnly = true)
    public DictionaryItem find(Integer id) {
        return dictionaryItemDAO.find(id);
    }

    /**
     * Получает элемент какого либо справочника
     * @param enumItem один из элементов енума
     * @return объект справочника
     */
    public DictionaryItem find(TSEnum enumItem) {
        return dictionaryItemDAO.find(enumItem.getId());
    }

    @Transactional(readOnly = true)
    public DictionaryItem find(Integer id, Integer dictionaryId) {
        return dictionaryItemDAO.find(id, dictionaryId);
    }

    @Transactional(readOnly = true)
    public List<DictionaryItem> getWorkplaces() {
        return dictionaryItemDAO.getItemsByDictionaryIdAndOrderById(DictionaryEnum.WORKPLACE.getId());
    }

    @Transactional(readOnly = true)
    public List<DictionaryItem> getOvertimeCauses() {
        return dictionaryItemDAO.getItemsByDictionaryIdAndOrderById(DictionaryEnum.OVERTIME_CAUSE.getId());
    }

    @Transactional(readOnly = true)
    public List<DictionaryItem> getUnfinishedDayCauses() {
        List<DictionaryItem> dictionaryList = dictionaryItemDAO.getItemsByDictionaryIdAndOrderById(DictionaryEnum.UNDERTIME_CAUSE.getId());
        List<DictionaryItem> dictionaryListClone = new ArrayList<DictionaryItem>(dictionaryList);
        for (DictionaryItem di : dictionaryListClone){
            if (di.getId().equals(UndertimeCausesEnum.OTHER.getId())){
                dictionaryList.remove(di);
                dictionaryList.add(di);
            }
        }
        return dictionaryList;
    }

    @Transactional(readOnly = true)
    public List<DictionaryItem> getItemsByDictionaryId(int dictId) {
        return dictionaryItemDAO.getItemsByDictionaryIdAndOrderById(dictId);
    }

    public String getDictionaryItemsInJson(List<DictionaryItem> items) {
        final JsonArrayNodeBuilder builder = anArrayBuilder();

        for (DictionaryItem item : items) {
            builder.withElement(
                    anObjectBuilder().
                            withField("id", JsonUtil.aStringBuilderNumber(item.getId())).
                            withField("value", aStringBuilder(item.getValue()))
            );
        }

        return JsonUtil.format(builder);
    }

    public String getDictionaryItemsInJson(int dictId) {
        return getDictionaryItemsInJson(getItemsByDictionaryId(dictId));
    }

    public List<DictionaryItem> getDictionaryItemsByEnumElements(List<TSEnum> enumElements){
        List<DictionaryItem> result = new ArrayList<DictionaryItem>(enumElements.size());
        for ( TSEnum element : enumElements){
            result.add(find(element.getId()));
        }
        return result;
    }

    public List<Integer> getDictItemsIdByEnumElements(List<TSEnum> enumElements){
        return getDictionaryItemsIds(getDictionaryItemsByEnumElements(enumElements));
    }

    public List<Integer> getDictionaryItemsIds(List<DictionaryItem> dictionaryItemList){
        List<Integer> result = new ArrayList<Integer>(dictionaryItemList.size());
        for ( DictionaryItem element : dictionaryItemList){
            result.add(element.getId());
        }
        return result;
    }
}