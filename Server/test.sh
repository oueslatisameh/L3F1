#!/bin/zsh

for i in 1 2 3 4 5
do
   ab -n 100 -c 20 -C name="s:web:d519f0e48b828819234f982622800608592e1d7be5ea2073b9b900eadb34c4a13ba0e06df986ee7a06dee42d96cbf28040a1dbbe5502eaf39db824de056fc3db06e9f6cede9618a61a5738e7de8cb343d1fc6f1fc8f46664a58b75f632fad0db5aac27ea54586721e4a6bb6a4fcb29713706f21832910eedfdd4246667110742aa7341be1f0a04150fc00cfdffc49b6f43736f94a49fa9da9a97c1c74e68df52e760151d3fafa3b6ec63579843805af55f364de88ab7fedf6c86d6557d8a04b500934d1951853630df418ad6b871c18bdd7acf3a6eb5da7cd4b18e7629699df9241abd192d25de233fb149914dd15cc2271f745f28f9748fbd70d90f2b6f15cb.HxRsUoppNwphDxV+EBEsYby8r+fDG0FcdelVqzigkU8" http://127.0.0.1:3000/list/name/`echo $RANDOM`
done