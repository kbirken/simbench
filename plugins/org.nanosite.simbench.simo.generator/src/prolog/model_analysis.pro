%
% model_analysis.pro - various analysis of the dependency graph derived from a hbsim model
%
% 2009-07-17 kbirken: initial version
%

% ---------------------------------------------------------------------------------------------------

% import generated module 'model'
:- include('../../src-gen/model.pro').

% import helpers
:- include('helpers.pro').


% ---------------------------------------------------------------------------------------------------

% operations on dependency graph

% adding artificial power-on step
action(power_on, 0, 0).
action(S, T1, T2) :- action_properties(S, T1, T2).

% start_step/1 is true for all initial steps of any plan
start_step(S) :- step(_,S), findall(P,plan(P,S),[]).

% next/2 defines graph edges in any kind of dependency
next(power_on,X) :- start_step(X).
next(X,Y) :- plan(X,Y).
next(X,Y) :- precondition(Y,X).
%next(X,Y) :- precondition_opt(Y,X).
next(X,Y) :-
	step(_,X),
	step(_,Y),
	findall(Res, (io_read(Y, _, _, ResIf), resource_interface(Res, ResIf, _, _, _), resource_precondition(Res,X)), Resources),
	list_to_set(Resources,RSet),
	member(_Resource,RSet).

% predecessors/2 computes all predecessors of a step in global dependency graph
predecessors(S,Ps) :- findall(X,next(X,S),Ps).

% predecessors_set/2 computes all predecessors of all steps in a set
predecessors_set([S],Ps) :- predecessors(S,Ps).
predecessors_set([S|Ss],Ps) :- predecessors(S,Ps1), predecessors_set(Ss,Ps2), union(Ps1,Ps2,Ps).

% chain/3 computes the chain from one step to another in global dependency graph
chain_aux(X,X,[]).
chain_aux(X,Z,[Y|Chain]) :- next(X,Y), chain_aux(Y,Z,Chain).
chain(X,Z,[X|Chain]) :- chain_aux(X,Z,Chain).

% all_predecessors/2 computes all predecessors (transitively)
all_predecessors_aux([],Visited,Visited).
all_predecessors_aux([X|Xs],Visited,Visited) :-
	predecessors_set([X|Xs],[]).
all_predecessors_aux(Xs,Visited,VisitedNew) :-
	predecessors_set(Xs,Ps),
	length(Ps,LPs), LPs>0,
	subtract(Ps,Visited,NewPs),
	union(Visited,NewPs,VNew),
	all_predecessors_aux(NewPs,VNew,VisitedNew).
all_predecessors(X,Ps) :-
	all_predecessors_aux([X], [], Ps).


% ---------------------------------------------------------------------------------------------------

% computation of derived data

% cpu_time/2 gives cpu time needed for a step
cpu_time(S,T) :- action(S,T,_).

% wait_time/2 gives wait time needed for a step
wait_time(S,T) :- action(S,_,T).


% single_io_time/2 gives the time for a single I/O operation
single_io_time(S, Res, Content, T) :-
	io_read(S, Content, Amount, RI),
	resource_interface(Res, RI, Bandwidth, _, _),
	T is Amount / Bandwidth * 1000.

% io_time/3 gives the time for all I/O operations of a step
io_time(S, Res, T) :- resource(Res,_), findall(T1, single_io_time(S, Res, _, T1), Ts), sumlist(Ts, T).

% io_time_max/2 gives the maximum of all I/O ops over all resources
io_time_max(S, T) :- findall(Tr, io_time(S,_,Tr), Trs), max_list(Trs, T).

% single_io_cpu_time/2 gives cpu time induced by a single I/O operation for a step
single_io_cpu_time(S, Content, T) :-
	io_read(S, Content, Amount, RI),
	resource_interface(_, RI, Bandwidth, InducedCPU, _),
	T_io is Amount / Bandwidth * 1000,
	T is T_io * (InducedCPU/100).

% io_cpu_time/2 gives cpu time induced by all I/O operations of a step
io_cpu_time(S,T) :- findall(T1, single_io_cpu_time(S, _, T1), Ts), sumlist(Ts, T).


% cpu_time_incl_induced/2 gives cpu time needed for a step including induced CPU by I/O operations
cpu_time_incl_induced(S,T) :-
	cpu_time(S,T1),
	io_cpu_time(S,T2),
	T is T1 + T2.


% max_local_time/2 gives maximum of times needed for a step
max_local_time(S,T) :-
	cpu_time_incl_induced(S,T1),
	wait_time(S,T2),
	io_time_max(S,T3),
	max_list([T1,T2,T3], T).


% cpu_time_incl_induced_list/2 gives sum of cpu times (incl. induced) for a list of steps
cpu_time_incl_induced_list([],0).
cpu_time_incl_induced_list([X|Xs],T) :- cpu_time_incl_induced(X,T1), cpu_time_incl_induced_list(Xs,T2), T is T1+T2.
	
% wait_time_list/2 gives sum of wait times for a list of steps
wait_time_list([],0).
wait_time_list([X|Xs],T) :- wait_time(X,T1), wait_time_list(Xs,T2), T is T1+T2.
	
% io_time_list/2 gives sum of I/O times for a list of steps
io_time_list([],_Res,0).
io_time_list([X|Xs],Res,T) :- io_time(X,Res,T1), io_time_list(Xs,Res,T2), T is T1+T2.
	
% max_local_time_list/2 gives sum of max_local_times for a list of steps
max_local_time_list([],0).
max_local_time_list([X|Xs],T) :- max_local_time(X,T1), max_local_time_list(Xs,T2), T is T1+T2.
	

% ---------------------------------------------------------------------------------------------------

% analysis: maximum global sum for each resource

cpu_time_incl_induced_all(S,T) :- all_predecessors(S,Ps), cpu_time_incl_induced_list([S|Ps],T).
	
wait_time_all(S,T) :- all_predecessors(S,Ps), wait_time_list([S|Ps],T).
	
io_time_all(S,Res,T) :- all_predecessors(S,Ps), io_time_list([S|Ps],Res,T).

	
% ---------------------------------------------------------------------------------------------------


subchain(Long,Short) :-
	append([_Prefix,Short,_Suffix], Long),
	length(Short,L), L>0.

relevant_sub(Long,Short,T) :-
	subchain(Long,Short),
	length(Short,L), L>1,
	skip_head_and_tail(Short,ShortInner),
	cpu_time_incl_induced_list(ShortInner,T),
	max_local_time_list(ShortInner,T),
	T > 0.

alternative(Long,Short,AlterInner,Tregular,Tadd) :-
	relevant_sub(Long,Short,Tregular),
	nth0(0,Short,S1),
	last(Short,S2),
	chain(S1,S2,Alter),
	skip_head_and_tail(Alter,AlterInner),
	intersection(AlterInner,Short,[]),
	cpu_time_incl_induced_list(AlterInner,Tadd).

improvement(Long,Short,AlterInner,Tregular,Tadd) :-
	alternative(Long,Short,AlterInner,Tregular,Tadd),
	Tadd > 0.



% ---------------------------------------------------------------------------------------------------

% analysis: worst path of worst local time
	
% tmaximum/2 computes the maximum T of structure t(_,T,_)	
tmaximum([X], X).
tmaximum([t(S,X,Y,Chain)|Xs], t(S,X,Y,Chain)) :- tmaximum(Xs, t(_,Max,_,_)), X@>Max, !.
tmaximum([_|Xs], Max) :- tmaximum(Xs, Max).


% sequential_deviation/4
sequential_deviation(E,Chain,T_chain,T_correction) :-
	all_predecessors(E,Ps),
	subtract(Ps,Chain,Others),
	cpu_time_incl_induced_list(Others, T_Others_cpu),
	cpu_time_incl_induced_list(Chain, T_chain_cpu),
	T_chain_no_cpu is T_chain - T_chain_cpu,
	T_correction is T_Others_cpu - T_chain_no_cpu.
	
% sequential_correction/4 accepts only positive deviations
sequential_correction(E,Chain,T_chain,T_chain) :-
	sequential_deviation(E,Chain,T_chain,T_correction),
	T_correction =< 0.
sequential_correction(E,Chain,T_chain,T_corrected) :-
	sequential_deviation(E,Chain,T_chain,T_correction),
	T_correction > 0,
	T_corrected is T_chain + T_correction.
	

% time_predecessor/6 computes the time needed for reaching a predecessor
%    step for a given step. This function has several modes:
%    
%    worst_path:  Only the worst path is taken into account
%    cpu_correct: The influences of cpu usage not on the worst
%                 path is also computed.
% 
time_predecessor(worst_path,_,P,T_chain,Chain,_) :-
	worst_path_time_aux(worst_path,P,T_chain,_,Chain).
time_predecessor(cpu_correct, E,P,T,Chain,T_correction) :-
	worst_path_time_aux(cpu_correct,P,T_chain,_,Chain),
	sequential_correction(E,Chain,T_chain,T),
	T_correction is T - T_chain.


% time_predecessors/2 computes the maximum time along the worst chain for each step of the input list
time_predecessors(_,_,[],[]).
time_predecessors(Mode,E,[P|Ps],[t(P,T,T_correction,Chain)|Ts]) :-
	time_predecessor(Mode,E,P,T,Chain,T_correction),
	time_predecessors(Mode,E,Ps,Ts).


% worst_path_time_aux/3 computes the maximum time along the worst chain for a given step
worst_path_time_aux(_,E,T,0,[]) :-
	predecessors(E,[]),
	max_local_time(E,T).
worst_path_time_aux(Mode,E,T,T_correction,[P|Chain]) :-
	predecessors(E,Ps),
	length(Ps,LPs), LPs>0,
	time_predecessors(Mode,E,Ps,Ts),
	tmaximum(Ts, t(P,Tmax,T_correction,Chain)),
	max_local_time(E,TE),
	T is Tmax+TE.


% worst_path_time/3 is tchain/3 with a better output of Chain
worst_path_time(E,T,Chain) :-
	worst_path_time_aux(worst_path,E,T,_,Es),
	reverse([E|Es],Chain).

% worst_path_time/3 is tchain/3 with a better output of Chain
worst_path_time(E,T,T_correction,Chain) :-
	worst_path_time_aux(cpu_correct,E,T,T_correction,Es),
	reverse([E|Es],Chain).


% ---------------------------------------------------------------------------------------------------

sep :- print('-----------------').

print_chain([]).
print_chain([X|Xs]) :-
	cpu_time_incl_induced(X,T1),
	wait_time(X,T2),
	io_time_max(X,T3),
	max_local_time(X,T4),
	format('   ~a   ~t~0f~42| ~t~0f~50| ~t~0f~58| ~t~0f~66|', [X, T1,T2,T3,T4]),
	nl,
	print_chain(Xs).


% ---------------------------------------------------------------------------------------------------

print_io_times(_M,[]).
print_io_times(M,[R|Rs]) :-
	io_time_all(M,R,T), T>0,
	format('Sum of ~a I/O time: ~t~3f~45|~n', [R,T]),
	print_io_times(M,Rs).
print_io_times(M) :-
	findall(R,resource(R,_),Rs),
	print_io_times(M,Rs).
	
analyse_milestone(M) :-
	sep, print(' Milestone '), print(M), print(' '), sep, nl,
	cpu_time_incl_induced_all(M,T1),
	format('Sum of cpu time: ~t~3f~45|~n', [T1]),
	wait_time_all(M,T2),
	format('Sum of wait time: ~t~3f~45|~n', [T2]),
	print_io_times(M),
	worst_path_time(M,T4,Chain),
	format('Worst path time: ~t~3f~45|~n', [T4]),
	print_chain(Chain), nl,
	worst_path_time(M,T5,T5_correction,Chain_corrected),
	format('Worst path time corrected: ~t~3f~45| ~t~3f~58|~n', [T5,T5_correction]),
	print_chain(Chain), nl, nl.

analyse_milestones([]).
analyse_milestones([M|Ms]) :- analyse_milestone(M), analyse_milestones(Ms).


% main clause just for easy testing
main :-
	findall(M, milestone(M), Ms),
	%analyse_milestones(Ms),
	analyse_milestone(s_MilestoneCollector_ready).


test :- analyse_milestones([s_HDD_FS_spawn_done]).

