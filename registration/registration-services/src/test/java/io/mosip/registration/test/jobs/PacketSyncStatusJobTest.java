package io.mosip.registration.test.jobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import io.mosip.registration.dao.SyncJobConfigDAO;
import io.mosip.registration.dto.ErrorResponseDTO;
import io.mosip.registration.dto.ResponseDTO;
import io.mosip.registration.dto.SuccessResponseDTO;
import io.mosip.registration.entity.SyncJobDef;
import io.mosip.registration.exception.ConnectionException;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.exception.RegBaseUncheckedException;
import io.mosip.registration.jobs.BaseJob;
import io.mosip.registration.jobs.JobManager;
import io.mosip.registration.jobs.SyncManager;
import io.mosip.registration.jobs.impl.PacketSyncStatusJob;
import io.mosip.registration.service.config.impl.JobConfigurationServiceImpl;
import io.mosip.registration.service.packet.RegPacketStatusService;
import io.mosip.registration.service.packet.impl.RegPacketStatusServiceImpl;
import io.mosip.registration.service.sync.PacketSynchService;
import io.mosip.registration.service.sync.PreRegistrationDataSyncService;

/**
 * 
 * @author M1063027
 *
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*" })
@PrepareForTest({ JobConfigurationServiceImpl.class })
public class PacketSyncStatusJobTest {

	@Mock
	JobExecutionContext context;

	@InjectMocks
	PacketSyncStatusJob packetSyncStatusJob;

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Mock
	PreRegistrationDataSyncService preRegistrationDataSyncService;

	@Mock
	private ApplicationContext applicationContext;

	@Mock
	SyncManager syncManager;

	@Mock
	private SyncJobConfigDAO jobConfigDAO;

	@Mock
	JobManager jobManager;

	@Mock
	JobDetail jobDetail;

	@Mock
	JobDataMap jobDataMap;

	HashMap<String, SyncJobDef> jobMap = new HashMap<>();

	@Mock
	BaseJob baseJob;

	@Mock
	private RegPacketStatusServiceImpl regPacketStatusService;

	private LinkedList<SyncJobDef> syncJobList;

	@Before
	public void intiate() {
		syncJobList = new LinkedList<>();
		SyncJobDef syncJob = new SyncJobDef();
		syncJob.setId("1234");

		syncJob.setApiName("packetSyncStatusJob");
		syncJob.setSyncFreq("0/5 * * * * ?");
		syncJobList.add(syncJob);

		syncJobList.forEach(job -> {
			jobMap.put(job.getId(), job);
		});
		Mockito.when(jobConfigDAO.getActiveJobs()).thenReturn(syncJobList);

		PowerMockito.mockStatic(JobConfigurationServiceImpl.class);

		Map<String, SyncJobDef> parentJobMap = new HashMap<>();
		parentJobMap.put("1", syncJob);
		Mockito.when(JobConfigurationServiceImpl.getParentJobMap()).thenReturn(parentJobMap);
	}

	@Test
	public void executeinternalTest() throws JobExecutionException, RegBaseCheckedException, ConnectionException {

		SyncJobDef syncJob = new SyncJobDef();
		syncJob.setId("1");

		Map<String, SyncJobDef> jobMap = new HashMap<>();

		jobMap.put(syncJob.getId(), syncJob);

		syncJob.setId("2");
		syncJob.setParentSyncJobId("1");

		jobMap.put("2", syncJob);

		ResponseDTO responseDTO = new ResponseDTO();
		SuccessResponseDTO successResponseDTO = new SuccessResponseDTO();
		responseDTO.setSuccessResponseDTO(successResponseDTO);

		Mockito.when(context.getJobDetail()).thenReturn(jobDetail);
		Mockito.when(jobDetail.getJobDataMap()).thenReturn(jobDataMap);
		Mockito.when(jobDataMap.get(Mockito.any())).thenReturn(applicationContext);
		Mockito.when(applicationContext.getBean(SyncManager.class)).thenReturn(syncManager);
		Mockito.when(applicationContext.getBean(JobManager.class)).thenReturn(jobManager);

		Mockito.when(applicationContext.getBean(RegPacketStatusService.class)).thenReturn(regPacketStatusService);

		Mockito.when(applicationContext.getBean(RegPacketStatusServiceImpl.class)).thenReturn(regPacketStatusService);

		Mockito.when(jobManager.getJobId(Mockito.any(JobExecutionContext.class))).thenReturn("1");

		Mockito.when(applicationContext.getBean(Mockito.anyString())).thenReturn(packetSyncStatusJob);
		Mockito.when(regPacketStatusService.syncServerPacketStatusWithRetryWrapper(Mockito.anyString()))
				.thenReturn(responseDTO);

		packetSyncStatusJob.executeInternal(context);
		packetSyncStatusJob.executeJob("User", "1");

	}
	
	@Test(expected = RegBaseUncheckedException.class)
	public void executejobNoSuchBeanDefinitionExceptionTest() {
		ResponseDTO responseDTO = new ResponseDTO();
		SuccessResponseDTO successResponseDTO = new SuccessResponseDTO();
		responseDTO.setSuccessResponseDTO(successResponseDTO);
		Mockito.when(context.getJobDetail()).thenThrow(NoSuchBeanDefinitionException.class);
		packetSyncStatusJob.executeInternal(context);
	}

	@Test(expected = RegBaseUncheckedException.class)
	public void executejobNullPointerExceptionTest() {
		ResponseDTO responseDTO = new ResponseDTO();
		SuccessResponseDTO successResponseDTO = new SuccessResponseDTO();
		responseDTO.setSuccessResponseDTO(successResponseDTO);
		Mockito.when(context.getJobDetail()).thenThrow(NullPointerException.class);

		packetSyncStatusJob.executeInternal(context);
	}
	
	
	  @Test(expected = RuntimeException.class) 
	  public void executejobRunTimeExceptionTest() throws ConnectionException,RegBaseCheckedException { 
	  ResponseDTO responseDTO = new ResponseDTO(); 
	  ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO();
	  List<ErrorResponseDTO> errorResponseDTOs = new ArrayList<>();
	  errorResponseDTOs.add(errorResponseDTO);
	  responseDTO.setErrorResponseDTOs(errorResponseDTOs); SyncJobDef syncJob = new
	  SyncJobDef(); syncJob.setId("1");
	  
	  Map<String, SyncJobDef> jobMap = new HashMap<>();
	  
	  jobMap.put(syncJob.getId(), syncJob);
	  
	  syncJob.setId("2"); syncJob.setParentSyncJobId("1");
	  
	  jobMap.put("2", syncJob);
	  
	  Mockito.when(context.getJobDetail()).thenReturn(jobDetail);
	  Mockito.when(jobDetail.getJobDataMap()).thenReturn(jobDataMap);
	  Mockito.when(jobDataMap.get(Mockito.any())).thenReturn(applicationContext);
	  Mockito.when(applicationContext.getBean(SyncManager.class)).thenReturn(syncManager);
	  Mockito.when(applicationContext.getBean(JobManager.class)).thenReturn(jobManager);
	  Mockito.when(applicationContext.getBean(RegPacketStatusService.class)).thenReturn(regPacketStatusService);
	  
	  Mockito.when(jobManager.getJobId(Mockito.any(JobExecutionContext.class))).thenReturn("1");
	  
	  Mockito.when(applicationContext.getBean(Mockito.anyString())).thenReturn(packetSyncStatusJob);
	  
	  Mockito.when(regPacketStatusService.syncServerPacketStatusWithRetryWrapper("System")).thenThrow(RuntimeException.class);
	  
	  packetSyncStatusJob.executeInternal(context);
	  
	  }
	 
}
