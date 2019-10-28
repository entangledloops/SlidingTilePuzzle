Example run of Solver with default settings:

```
     0 1 2
    ------
 0|  1 2 3
 1|  4 5 6
 2|  7 8 0

runs: 100
heuristic: avg moves
	manhattan: 255
	hamming: 1298
	random: 176127
```

This implements a simple greedy best-first search. It favors new moves or previously tried moves, 
and when there are no new moves left from a position, it chooses randomly.
