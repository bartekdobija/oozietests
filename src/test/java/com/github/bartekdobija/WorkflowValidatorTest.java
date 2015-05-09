package com.github.bartekdobija;

import static org.junit.Assert.assertEquals;

import org.apache.oozie.cli.OozieCLI;
import org.junit.Test;

public class WorkflowValidatorTest {
  
  @Test
  public void testValidation() {
    String[] args = new String[] { "validate",
    "src/main/resources/workflow.xml" };
    assertEquals(0, new OozieCLI().run(args));
  }
  
}
