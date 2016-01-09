
/* This function must be reentrant, because multiple function blocks
 * may call it parallely */
void execute3(unsigned int delay, /* in ms */
			 unsigned int sleep, /* in ms */
			 unsigned int interleave, /* in us, 0 means don't interleave */
			 QueueId clientsSystemQueue,
			 Peripheral peripheral1, QueueId queue1, unsigned int kb1,
			 Peripheral peripheral2, QueueId queue2, unsigned int kb2,
			 Peripheral peripheral3, QueueId queue3, unsigned int kb3)
{

	Job job1, job2, job3;
	DELAY(0, unit_us);

	/* send requests to peripherals */
	if (peripheral1 != NUM_PERIPHERALS)
	{
		job1.sema = newSemaphore(1);
		job1.queue = gc->queues[clientsSystemQueue];
		job1.kb = kb1;
		job1.peri = peripheral1;
		acquireSemaphore(job1.sema);
		sendQueue(gc->queues[queue1], (const char*)&job1, sizeof(job1));
	}

	if (peripheral2 != NUM_PERIPHERALS)
	{
		job2.sema = newSemaphore(1);
		job2.queue = gc->queues[clientsSystemQueue];
		job2.kb = kb2;
		job2.peri = peripheral2;
		acquireSemaphore(job2.sema);
		sendQueue(gc->queues[queue2], (const char*)&job2, sizeof(job2));
	}

	if (peripheral3 != NUM_PERIPHERALS)
	{
		job3.sema = newSemaphore(1);
		job3.queue = gc->queues[clientsSystemQueue];
		job3.kb = kb3;
		job3.peri = peripheral3;
		acquireSemaphore(job3.sema);
		sendQueue(gc->queues[queue3], (const char*)&job3, sizeof(job3));
	}

	/* convert delays to us */
	delay *= 1000;
	sleep *= 1000;

	/* determine delay and sleep part of timeslice */
	unsigned int deltaD, deltaS;
	if (interleave && (delay + sleep) != 0)
	{
		deltaD = interleave * delay / (delay + sleep);
		if (!deltaD)
			deltaD = 1;
		deltaS = interleave * sleep / (delay + sleep);
		if (!deltaS)
			deltaS = 1;
	} else
	{
		deltaD = delay;
		deltaS = sleep;
	}

	/* work the cpu interleaved */
	while (0 < delay || 0 < sleep)
	{
		unsigned int d = (delay < deltaD) ? delay : deltaD;
		unsigned int s = (sleep < deltaS) ? sleep : deltaS;

		/* actively eat cpu */
		if (0 < d)
		{
			DELAY(d, unit_us);
			delay -= d;
		}

		/* wait for hardware response. this is potentially wrong,
		 * because the sleep will be longer than necessary if there's
		 * more than one process currently running. */
		if (0 < s)
		{
			SLEEP(s, unit_us);
			sleep -= s;
		} else if (0 < delay || 0 < sleep)
		{
			schedule();
		}
	}

	/* wait for peripherals */
	if (peripheral1 != NUM_PERIPHERALS)
	{
		acquireSemaphore(job1.sema);
		deleteSemaphore(job1.sema);
	}

	if (peripheral2 != NUM_PERIPHERALS)
	{
		acquireSemaphore(job2.sema);
		deleteSemaphore(job2.sema);
	}

	if (peripheral3 != NUM_PERIPHERALS)
	{
		acquireSemaphore(job3.sema);
		deleteSemaphore(job3.sema);
	}
}

void execute2(unsigned int delay, /* in ms */
			 unsigned int sleep, /* in ms */
			 unsigned int interleave, /* in us, 0 means don't interleave */
			 QueueId clientsSystemQueue,
			 Peripheral peripheral1, QueueId queue1, unsigned int kb1,
			 Peripheral peripheral2, QueueId queue2, unsigned int kb2)
{
	execute3(delay, sleep, interleave,
			clientsSystemQueue,
			peripheral1, queue1, kb1,
			peripheral2, queue2, kb2,
			NUM_PERIPHERALS, 0, 0);
}


void execute1(unsigned int delay, /* in ms */
			 unsigned int sleep, /* in ms */
			 unsigned int interleave, /* in us, 0 means don't interleave */
			 QueueId clientsSystemQueue,
			 Peripheral peripheral1, QueueId queue1, unsigned int kb1)
{
	execute3(delay, sleep, interleave,
			clientsSystemQueue,
			peripheral1, queue1, kb1,
			NUM_PERIPHERALS, 0, 0,
			NUM_PERIPHERALS, 0, 0);
}

void execute0(unsigned int delay, /* in ms */
			 unsigned int sleep, /* in ms */
			 unsigned int interleave) /* in us, 0 means don't interleave */
{
	execute3(delay, sleep, interleave, 0,
			 NUM_PERIPHERALS, 0, 0,
			 NUM_PERIPHERALS, 0, 0,
			 NUM_PERIPHERALS, 0, 0);
}

void steady_state (unsigned int cpu /* in percent of one CPU node */)
{
	DELAY(0, unit_us);

	/* be robust (should be checked already on model level) */
	if (cpu>100) {
		cpu = 100;
	}

	/* we use cpu% of 100% CPU every 100 ms */
	unsigned int delay = cpu * 1000; /* in us */
	unsigned int sleep = 100000-delay; /* in us */

	/* use part of the cpu endlessly */
	for (;;)
	{
		/* actively eat cpu */
		DELAY(delay, unit_us);

		/* wait remaining time until 100ms are over */
		/* TODO: other processes will delay our compute time and thus reduce our steady state percentage */
		SLEEP(sleep, unit_us);
	}
}
