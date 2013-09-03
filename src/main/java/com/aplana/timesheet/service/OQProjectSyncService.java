package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.DivisionDAO;
import com.aplana.timesheet.dao.EmployeeDAO;
import com.aplana.timesheet.dao.ProjectDAO;
import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.enums.TypesOfActivityEnum;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import static com.aplana.timesheet.util.ExceptionUtils.getRealLastCause;

@Service("oqProgectSyncService")
public class OQProjectSyncService extends AbstractServiceWithTransactionManagement {

    private static final String DATE_FORMAT = "dd.MM.yyyy";

    private static final Logger logger = LoggerFactory.getLogger(OQProjectSyncService.class);
    private final StringBuffer trace = new StringBuffer();

    private static final List<String> newStatus = new ArrayList<String>() {
        {
            add("1-черновик");
            add("3-активный");
        }
    };

    private static final List<String> closedStatus = new ArrayList<String>() {
        {
            add("4-завершен");
            add("5-приостановлен");
            add("6-прекращен");
            add("7-архив");
        }
    };

    @Autowired
    private ProjectDAO projectDAO;

    @Autowired
    private EmployeeDAO employeeDAO;

    @Autowired
    private DivisionDAO divisionDAO;

    @Autowired
    EmployeeLdapService employeeLdapService;

    @Autowired
    private TSPropertyProvider propertyProvider;

    @Autowired
    private DictionaryItemService dictionaryItemService;

    private URL oqUrl;

    @PostConstruct
    private void Init() {
        try {
            String oqUrlLocal = propertyProvider.getOqUrl();
            if (oqUrlLocal != null && !oqUrlLocal.isEmpty())
                oqUrl = new URL(oqUrlLocal);
        } catch (MalformedURLException e) {
            logger.warn("OQ.url parameter has incorrect format");
        }
    }

    /*
     * Синхронизация проектов с аплановской системой ведения проектов
     * По адресу OQurl находится список проектов вместе с сотрудниками
     * в виде xml (файл синхронизации)
     *
     * <project id="" name="" description="" customer="" ending="" status="7-архив" pm="CN=Somebody Dmitry/CN=Users/DC=aplana/DC=com" hc="CN=Zaitsev Dmitry/CN=Users/DC=aplana/DC=com" >
     *     <workgroup>
     *         <user>Ivanov Ivan</user>
     *         <user>Smirnov Semen</user>
     *         <user>Sidorov Igor</user>
     *     </workgroup>
     * </project>
    */
    public void sync() {
        final TransactionStatus transactionStatus = getNewTransaction();
        try {
            trace.setLength(0);
            try {
                trace.append("Начало синхронизации\n");
                projectDAO.setTrace(trace);
                // получим список веток project из xml файла
                NodeList nodes = getOQasNodeList();
                trace.append("В файле синхронизации найдено: ").append(nodes.getLength()).append(" проектов\n");

                final DictionaryItem projectState = getProjectState();

                for (int i = 0; i < nodes.getLength(); i++) {
                    createOrUpdateProject(nodes.item(i).getAttributes(), projectDAO, projectState);
                }
                trace.append("Синхронизация завершена\n");
            } catch (Exception e) {
                logger.error("oq project sync error: ", e);
                trace.append("Синхронизация прервана из-за ошибки: ").append(getRealLastCause(e).getMessage()).append("\n");
            }

            commit(transactionStatus);

        } catch (Exception e) {
            if (transactionStatus != null) {
                rollback(transactionStatus);
            }
        }
    }

    public void createOrUpdateProject(NamedNodeMap nodeMap, ProjectDAO dao, DictionaryItem state) {
        final TransactionStatus transactionStatus = getNewTransaction();
        try {
            Project project = new Project();      // проект из БД

            // поля синхронизации
            String name = nodeMap.getNamedItem("name").getNodeValue().trim();    // название проекта
            String idProject = nodeMap.getNamedItem("id").getNodeValue();        // id проекта
            String status = nodeMap.getNamedItem("status").getNodeValue();       // статус проекта
            String pmLdap = nodeMap.getNamedItem("pm").getNodeValue();           // руководитель проекта
            String hcLdap = nodeMap.getNamedItem("hc").getNodeValue();           // hc

            // ищем в БД запись о проекте
            Project foundProject = dao.findByProjectId(idProject);
            if (foundProject == null) {  // если проекта еще нет в БД
                project.setActive(newStatus.contains(status)); // установим ему новый статус
                project.setEndDate(DateTimeUtil.stringToDate("01.01.2050", DATE_FORMAT)); // APLANATS-826 при добавлении проекта прописываем в качестве даты окончания фиксированную большую дату
            } else {
                // если проект уже существовал - статус менять не будем
                // см. //APLANATS-408

                // KSS. 02.07.2013. Если проект закрыт - закрываем, иначе - не меняем статус
                if (closedStatus.contains(status)) {
                    project.setActive(false);
                } else {
                    project.setActive(foundProject.isActive());
                }
                project.setCqRequired(foundProject.isCqRequired());
                project.setEndDate(foundProject.getEndDate()); // APLANATS-826
            }

            project.setName(name);
            project.setProjectId(idProject);
            project.setStartDate(DateTimeUtil.stringToDate(nodeMap.getNamedItem("begining").getNodeValue(), DATE_FORMAT));
            // APLANATS-826
            // project.setEndDate(DateTimeUtil.stringToDate(nodeMap.getNamedItem("ending").getNodeValue(), DATE_FORMAT));
            project.setState(state);

            if (project.isActive()) {
                if (!setPM(project, pmLdap)) {
                    return; //если не указан РП или его нет в БД, то проект не сохраняем, переходим к следующему
                }
                setDivisions(project, hcLdap, foundProject);  // установим подразделения, связанные с проектом
                setDivision(project, hcLdap, pmLdap);  // установим ответственное подразделение к проекту
            }
            dao.store(project); // запишем в БД

            if (transactionStatus != null) {
                commit(transactionStatus);
            }

        } catch (Exception e) {
            logger.error("createOrUpdateProject error", e);
            if (transactionStatus != null) {
                rollback(transactionStatus);
            }
        }
    }

    private DictionaryItem getProjectState() {
        return dictionaryItemService.find(TypesOfActivityEnum.PROJECT.getId());
    }

    private boolean setPM(Project project, String pmLdap) {
        //APLANATS-429
        if ((pmLdap == null) || (pmLdap.equals(""))) {
            trace.append("Проект ").append(project.getName()).append(" пропущен, т.к. не указан руководитель проекта \n");
            return false;
        }

        Employee projectLeader = this.employeeDAO.findByLdapName(pmLdap.split("/")[0]);
        if (projectLeader == null) {
            trace.append("Проект ").append(project.getName()).append(" проигнорирован, " +
                    "т.к. руководитель проекта ").append(pmLdap).append(" не найден в базе ldap\n");
            return false;
        }

        project.setManager(projectLeader);
        return true;
    }

    private void setDivisions(Project project, String hcLdap, Project foundProject) {
        Set<Division> divisions = new TreeSet<Division>();
        Employee employee = this.employeeDAO.findByLdapName(hcLdap.split("/")[0]);
        if (employee != null) {
            divisions.add(employee.getDivision());
        }
        if (foundProject != null) {
            Set<Division> foundProjectDivisions = foundProject.getDivisions();
            if (foundProjectDivisions != null) {
                divisions.addAll(foundProjectDivisions);
                for (Iterator<Division> iterator = foundProjectDivisions.iterator(); iterator.hasNext(); ) {
                    Division next = iterator.next();
                    if (divisions.contains(next))
                        divisions.add(next);
                }
            }
        }

        project.setDivisions(divisions);
    }

    private void setDivision(Project project, String hcLdap, String pmLdap) {
        if (hcLdap != null && !hcLdap.isEmpty()) {
        /* ищем человека в БД */
            Employee employee = this.employeeDAO.findByLdapName(hcLdap.split("/")[0]);
            if (employee != null) {
            /* нашли, ищем лидер ли он подразделения */
                Division division = this.divisionDAO.findByLeader(employee);
                if (division != null)
                /* он лидер, записываем в проект это подразделение */
                    project.setDivision(division);
            } else {
                if (pmLdap != null && !pmLdap.isEmpty()) {
                    /* ищем руководителя проекта по OQ данным */
                    Employee pm = this.employeeDAO.findByLdapName(pmLdap.split("/")[0]);
                    if (pm != null) {
                        project.setDivision(pm.getDivision());
                    } else {
                        trace.append("В проекте ")
                                .append(project.getName())
                                .append(" не установлено поле division (ответственное подразделение), т.к. не найдено руководителя подразделения ")
                                .append(hcLdap)
                                .append(" или руководитель проекта ")
                                .append(pmLdap)
                                .append("\n");
                    }
                }
            }
        }

    }

    public NodeList getOQasNodeList() throws SAXException,
            ParserConfigurationException,
            XPathExpressionException {
        NodeList result = null;
        if (oqUrl == null) {
            logger.warn("OQ.url not found. Synchronization impossible.");
            trace.append("Синхронизация невозможна: OQ.url не указан.");
            return result;
        }
        try {
            URLConnection oqc = oqUrl.openConnection();
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(oqc.getInputStream());
            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression expr = xpath.compile("/root/projects/project");
            result = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        } catch (IOException e) { // обрабатываем только IOException - остальные выбрасываем наверх
            logger.error("oq project sync error: ", e);
            trace.append("Синхронизация прервана из-за ошибки ввода/вывода при попытке получить и прочитать файл " +
                    "синхронизации: ").append(e.getMessage()).append("\n");
        }

        return result;
    }

    public String getTrace() {
        return trace.toString();
    }
}
