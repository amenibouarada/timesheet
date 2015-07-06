Система списания занятости
==========================

Общее назначение системы
------------------------

Система списания занятости предназначена для ежедневного учета трудозатрат сотрудников организации на выполнение работ
по различным проектам. Предусмотрена возможность детализации работ по проектам и отдельным задачам. Система позволяет
контролировать своевременность заполнения сотрудниками отчетности с учетом организационных ролей и иерархической
структуры управления организацией. Существует возможность формирования аналитических отчетов за различные периоды в
разрезах подразделения, проекта или отдельного сотрудника.

Авторизация в системе реализована с помощью сервера OpenLDAP. Используемая СУБД - PostgresSQL 9.1.
Рассылка уведомлений сотрудникам возможна с помощью любого внешнего smtp сервера.

Ознакомиться с возможностями системы можно [здесь](http://softdev.it.ru/home/development-areas/open-source/timesheet.html)

[Ответы на часто задаваемые вопросы](https://github.com/itru/timesheet/wiki/FAQ)

Получение проекта с Github
--------------------------

Для получения файлов проекта, необходимо установить приложение git (http://git-scm.com/) и произвести клонирование
структуры проекта командой в командной строке gitBash:

    git clone https://github.com/itru/timesheet.git

Сборка проекта с использованием Maven
-------------------------------------

Приложение использует фреймворк для автоматизации сборки Maven (http://maven.apache.org/). Используемая для сборки
версия Maven – 3.2 или более новая. Поле установки Maven, необходимо создать пользовательские переменные окружения
JAVA\_HOME, M2\_HOME и M2 содержащие соответственно пути до файлов JDK и Maven. Пример содержимого переменных:

    JAVA_HOME : «C:\Program Files (x86)\Java\jdk1.7.0_51»
    M2_HOME   : «C:\Developer\apache-maven-3.2.1»
    M2        : «%M2_HOME%\bin»

Также в переменную окружения path добавить через точку с запятой значение:

    %M2%; %JAVA_HOME%\bin

Необходимо добавить в локальный репозиторий Maven библиотеку padeg, предназначенную для обработки фамилий, имён,
отчеств (склонения их по падежам). Для этого нужно исполнить команду:

    mvn install:install-file -Dfile=[path-to-pageg]padeg-3.3.0.24.jar -DgroupId=lib.padeg -DartifactId=padeg -
    Dversion=3.3.0.24 -Dpackaging=jar

В данной команде [path-to-pageg] - путь к каталогу, в котором находится библиотека padeg (каталог проекта
src\main\webapp\WEB-INF\lib). Пример пути:

     D:\workspace\timesheet\src\main\webapp\WEB-INF\lib

Пример команды с указанным путём:

     mvn install:install-file -Dfile=D:\workspace\timesheet\src\main\webapp\WEB-INF\lib\padeg-3.3.0.24.jar
     -DgroupId=lib.padeg -DartifactId=padeg -Dversion=3.3.0.24 -Dpackaging=jar
    
Для сборки war-архива веб-приложения необходимо в корневой папке проекта исполнить команду:

    mvn clean package

Локальный запуск приложения для задач разработки и отладки производится при помощи запуска файла «run.bat».
В результате исполнения файла будет запущен локальный сервер Jetty, а само приложение будет доступно
по адресу http://localhost:8080.

База данных PostgreSQL
----------------------

Для хранения данных приложения используется БД PostgreSQL версии 9.1. После установки необходимо добавить путь
до исполняемых файлов СУБД в пользовательскую переменную окружения PATH. Пример пути:

    C:/Developer/PostgreSQL/9.1/bin

В файле pg_hba.conf для строки:

    host    all             all             127.0.0.1/32            ident
необходимо изменить метод идентификации с ident на md5.

Расположения файла:
* linux: /var/lib/pgsql/9.1/data/pg_hba.conf
* windows: C:\Program Files\PostgreSQL\9.1\data\pg_hba.conf

Затем необходимо произвести восстановление данных из дампа, распаковав архив «timesheet\_db\_dump.zip» и запустив
файл «restore.bat». В результате исполнения файла будет создана база данных с именем `time_sheet`, содержащяя данные
приложения.

Для соединения приложения с базой данных необходимо изменить параметры в файле настроек «timesheet.properties»
следующим образом:

    db.username=postgres
    db.password=postgres
    db.driver=org.postgresql.Driver
    db.url=jdbc\:postgresql\://localhost\:5432/time_sheet
    db.dialect=org.hibernate.dialect.PostgreSQLDialect

LDAP-сервер
-----------

Для работы системы авторизации необходимо соединение с LDAP сервером. Для нужд разработки достаточно использования
локального OpenLDAP сервера (http://www.userbooster.de/en/download/openldap-for-windows.aspx). После установки сервера
необходимо остановить Windows-службу «OpenLDAP Service», переместить файлы из архива «LDAP.zip» в папку установки
OpenLDAP, затем заново запустить сервис.

Необходимо сконфигурировать подключение к OpenLDAP, это следующие настройки (в файле настроек «timesheet.properties»):

	ldap.userDn=cn=Manager,dc=example,dc=com
	ldap.password=secret
	ldap.base=dc=example,dc=com
	ldap.url=ldap://localhost:389
	ldap.domain=example.com
	ldap.search.pattern=(uid={0})

Если OpenLDAP установлен на локальном компьютере, то можно оставить конфигурацию выше без изменений.
Так же для конфигурации необходимо раскомментировать строки с маппингом атрибутов для OpenLDAP:

	#ldap.field.division=departmentNumber
	#ldap.field.displayName=displayName
	#ldap.field.email=mail
	#ldap.field.manager=manager
	#ldap.field.title=title
	#ldap.field.whenCreated=createTimestamp
	#ldap.field.city=l
	#ldap.field.mailNickname=mailNickname
	#ldap.field.ldapCn=distinguishedname
	#
	#ldap.field.objectClass=objectClass
	#ldap.objectClass.employee=person
	#ldap.objectClass.disabledEmployee=person
	#ldap.objectClass.division=group
	#ldap.ou.disabledEmployee=Disabled Users
	#ldap.cn.division=_Project Center *
	#
	#ldap.field.SID=uid
	#ldap.field.divisionName=description
	#ldap.field.leader=managedBy

В LDAP прописаны следующие пользователи:

*   Роли сотрудников: AnalystA1, AnalystA2, DeveloperB1, DeveloperB2, DeveloperB3, TesterC1, TesterC2, TesterC3.
    Сотрудники имеют права на списание занятости и просмотр истории списаний.
*   Роли менеджера: ManagerA, ManagerB, ManagerC. Менеджеры имеют права на списание занятости и формирование
    аналитических отчетов.
*   Роль администратора: Boss. Администратор имеет права на формирование аналитических отчетов и выполнение
    служебных функций (синхронизация и рассылка уведомлений).

Пароли идентичны логинам.

В файле «root-context.xml» секцию "Реализация авторизации в системе" необходимо изменить следующим образом:

    <!-- Реализация авторизации в системе -->
    <import resource="app/authentication_OpenLDAP.xml"/>
    <!--<import resource="app/authentication_ActiveDirectory.xml"/>-->

Сервер электронной почты
------------------------

Для функционирования системы рассылки уведомлений пользователям необходимо настроить подключение к почтовому серверу.
В файле «timesheet.properties» для этого предназначены следующие свойства:

    mail.send.enable=false – глобальный флаг, позволяет отключить email рассылку
    mail.transport.protocol=smtp – протокол
    mail.smtp.host=smtp.yandex.ru – адрес почтового сервера
    mail.smtp.auth=true – необходимость авторизации при отправке сообщений
    mail.smtp.port=25 – порт почтового сервера
    mail.username=username – имя почтового аккаунта
    mail.password=userpassword – пароль

Значения этих параметров могут различаться у различных почтовых хостеров. При значении параметра
`mail.send.enable=false` и установленном значении параметра `mail.debug.address` отправка всех генерируемых системой
писем будет производиться в режиме отладки на адрес, указанный в параметре `mail.debug.address`.

Таск-трекер JIRA
----------------

Для связи приложения с трекером JIRA в файле «timesheet.properties» присутствуют следующие параметры:

    jira.server.url=http://jira.example.com – адрес JIRA
    jira.username=manager – логин администратора JIRA
    jira.password=password – пароль администратора JIRA

Смена дизайна и локализация
---------------------------

Все используемые приложением стили и изображения находятся в папке проекта «src\main\webapp\resources», их изменения
достаточно для смены внешнего вида приложения.

Текстовые строки находятся в папке «src\main\webapp\WEB-INF\messages»: файл «errors.properties» содержит строки
сообщений об ошибках, файл «messages.properties» содержит строки, присутствующие на страницах приложения.


