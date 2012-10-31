#ifndef __CPU_H
#define __CPU_H

#include <CLib.h>

#define setCondition(proc, event)				\
	STATE(event);								\
	EVENT(proc ## _ ## event);					\
	setEvent(gc->conditions[proc ## _ ## event])

#define getCondition(proc, event)						\
	if (!readEvent(gc->conditions[proc ## _ ## event]))	{	\
		waitEvent(gc->conditions[proc ## _ ## event], 1);	\
		resetEvent(gc->conditions[proc ## _ ## event]);		\
		setEvent(gc->conditions[proc ## _ ## event]);		\
	}

#endif

#define sendTrigger(fb, taskgroup, behaviour)	\
   { int msg = BHVR_ ## fb ## _ ## behaviour;      \
     sendQueue(gc->queues[QUEUE_ ## fb ## _TG ## taskgroup], (char*)&msg, sizeof(msg));  \
	} 
