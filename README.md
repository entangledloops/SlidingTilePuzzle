The Java source implements a simple greedy best-first search that never backs up. 
It favors new moves over previously tried moves, and when there are no new moves left from a position, it chooses randomly.

```
     0 1 2
    ------
 0|  1 2 3
 1|  4 5 6
 2|  7 8 0

runs: 10
heuristic: avg moves
	manhattan: 250
	euclidean: 436
	hamming: 1446
	random: 294180
```


For comparison, the Kotlin source implements A* to find optimal solutions. You can tweak the weight parameter in the source
to find bounded solutions to larger problems.

```
[3, 4, 8]
[1, 6, 0]
[2, 7, 5]

hamming solution found in 19 ms:
	iterations: 4973
	pathLen: 21
euclidean solution found in 3 ms:
	iterations: 861
	pathLen: 21
manhattan solution found in 1 ms:
	iterations: 473
	pathLen: 21
```

The output between the two implementations
isn't consistent because they were written independently and are posted here together for educational purposes. If I 
find the time, I'll come back and normalize the outputs later.
