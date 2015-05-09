package com.github.bartekdobija;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Properties;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.oozie.client.OozieClient;
import org.apache.oozie.client.WorkflowJob;
import org.apache.oozie.local.LocalOozie;
import org.apache.oozie.service.XLogService;
import org.apache.oozie.test.MiniOozieTestCase;

public class SampleOozieTest extends MiniOozieTestCase {
  
  @Override
  protected void setUp() throws Exception {
    System.setProperty("oozie.test.metastore.server", "false");
    System.setProperty(XLogService.LOG4J_FILE, "oozie-log4j.properties");
    super.setUp();
  }
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }
  
  public void testWorkflowRun() throws Exception {
    String wfApp = "<workflow-app xmlns='uri:oozie:workflow:0.1' name='test-wf'>"
        + "<start to='end'/><end name='end'/></workflow-app>";
    
    FileSystem fs = getFileSystem();
    Path appPath = new Path(getFsTestCaseDir(), "app");
    fs.mkdirs(appPath);
    fs.mkdirs(new Path(appPath, "lib"));
    
    Writer writer = new OutputStreamWriter(fs.create(new Path(appPath,
        "workflow.xml")));
    writer.write(wfApp);
    writer.close();
    
    final OozieClient wc = LocalOozie.getClient();
    
    Properties conf = wc.createConfiguration();
    conf.setProperty(OozieClient.APP_PATH,
        new Path(appPath, "workflow.xml").toString());
    conf.setProperty(OozieClient.USER_NAME, getTestUser());
    
    final String jobId = wc.submit(conf);
    assertNotNull(jobId);
    
    WorkflowJob wf = wc.getJobInfo(jobId);
    assertNotNull(wf);
    assertEquals(WorkflowJob.Status.PREP, wf.getStatus());
    
    wc.start(jobId);
    
    waitFor(1000, new Predicate() {
      @Override
      public boolean evaluate() throws Exception {
        WorkflowJob wf = wc.getJobInfo(jobId);
        return wf.getStatus() == WorkflowJob.Status.SUCCEEDED;
      }
    });
    
    wf = wc.getJobInfo(jobId);
    assertNotNull(wf);
    assertEquals(WorkflowJob.Status.SUCCEEDED, wf.getStatus());
    
  }
  
}
