%
% helpers.pro - various helper predicates
%
% 2009-07-17 kbirken: initial version
%



% skip_head_and_tail/2: helper which removes the head and tail from a list
skip_head_and_tail([_|Long],Inner) :-
	reverse(Long,[_|InnerRev]),
	reverse(InnerRev, Inner).

