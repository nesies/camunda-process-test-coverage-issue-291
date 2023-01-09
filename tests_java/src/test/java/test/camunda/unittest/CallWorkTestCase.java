/*
 * vim: tabstop=4 expandtab shiftwidth=4 softtabstop=4
 * -*- coding: utf-8 -*-
 */
package test.camunda.unittest;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.ProcessEngine;

import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.history.HistoricExternalTaskLog;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRuleBuilder;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.complete;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.processInstanceQuery;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;

public class CallWorkTestCase {
    @Rule
    @ClassRule
    public static ProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().build();

    @Test
    @Deployment(resources = {
        "test/camunda/unittest/called.bpmn",
        "test/camunda/unittest/caller.bpmn"
        }
    )
    public void testCallDoWork() {
        Map<String, Object> variables;
	    variables = Variables.createVariables()
             .putValue("todo", true);
        
        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
	        "Process_caller",
	        variables);

        assertThat(processInstance).isActive();
        assertThat(processInstanceQuery().count()).isEqualTo(1);
  
        assertThat(processInstance).externalTask().isNotNull();
        assertThat(processInstance).isWaitingAt("Activity_task");
        complete(externalTask());

        assertThat(calledProcessInstance(processInstance)).isNotNull();
        assertThat(calledProcessInstance(processInstance)).isWaitingAt("Activity_do_work");

        complete(externalTask());

        assertThat(processInstance).isEnded();
    }
}
