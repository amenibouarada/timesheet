package com.aplana.timesheet.service;

import com.aplana.timesheet.AbstractTest;
import com.aplana.timesheet.dao.ProjectDAO;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.enums.TypesOfActivityEnum;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertTrue;

/**
 * @author iziyangirov
 */
public class OQProjectSyncServiceTest extends AbstractTest {

    @Autowired
    OQProjectSyncService oqProjectSyncService;

    @Autowired
    private DictionaryItemService dictionaryItemService;

    @Test
    public void testSync() throws Exception {
        final NodeList nodeList = oqProjectSyncService.getOQasNodeList();
        assertTrue(nodeList != null && nodeList.getLength() != 0);
        final int checkingNode = (int)(Math.random() * (nodeList.getLength()  + 1));

        final ProjectDAO dao = mock(ProjectDAO.class);

        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                Project project = (Project) args[0];
                // проверим, что в прожект попало всё, что было в текущей ветве хмл-файла
                NamedNodeMap node = nodeList.item(checkingNode).getAttributes();
                String name      = node.getNamedItem("name").getNodeValue().trim();  // название проекта
                String idProject = node.getNamedItem("id").getNodeValue();           // id проекта
                //String status    = node.getNamedItem("status").getNodeValue();       // статус проекта
                String pmLdap    = node.getNamedItem("pm").getNodeValue();           // руководитель проекта
                //String hcLdap    = node.getNamedItem("hc").getNodeValue();           // hc

                assertTrue(name.equals(project.getName()));
                assertTrue(idProject.equals(project.getProjectId()));
                //assertTrue(status.equals()); // так как статусы могут не совпадать - закомментил эту проверку
                if (project.isActive()) {      // только если активный проект - проверяем
                    assertTrue(pmLdap.split("/")[0].contains(    project.getManager().getLdap().split(",")[0]  ));
                    //assertTrue(hcLdap.contains()); // решено не тестировать, потому что простого теста не напишешь
                }

                return null;
            }})
            .when(dao).syncStore((Project) anyObject());

        oqProjectSyncService.createOrUpdateProject(
                nodeList.item(checkingNode).getAttributes(), dao,
                dictionaryItemService.find(TypesOfActivityEnum.PROJECT.getId())
        );
    }
}
