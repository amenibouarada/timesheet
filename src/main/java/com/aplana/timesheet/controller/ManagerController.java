package com.aplana.timesheet.controller;


import com.aplana.timesheet.system.properties.TSPropertyProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = {"/managertools/", "/managertools"})
public class ManagerController {
	private static final Logger logger = LoggerFactory.getLogger(ManagerController.class);

    @Autowired
    private TSPropertyProvider propertyProvider;

    @RequestMapping
    public String managerPanel( Model model ) {
        String pentahoPath = propertyProvider.getPentahoUrl();

        if(StringUtils.isBlank(pentahoPath)) {
            pentahoPath = null;
            logger.warn("In your properties not assign 'pentaho.url', some functions will be disabled");
        }

		model.addAttribute("pentahoUrl", pentahoPath);
        return "managerPanel";
    }
}

