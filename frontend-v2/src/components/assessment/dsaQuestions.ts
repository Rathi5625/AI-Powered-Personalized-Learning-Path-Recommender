export interface AssessmentQuestion {
  id: number;
  questionNumber: number;
  question: string;
  options: {
    id: 'A' | 'B' | 'C' | 'D';
    text: string;
  }[];
  correctAnswer: 'A' | 'B' | 'C' | 'D';
  topic: string;
}

export const DSA_MOCK_QUESTIONS: AssessmentQuestion[] = [
  {
    id: 1,
    questionNumber: 1,
    question: 'What is the best-case time complexity of linear search in an array?',
    options: [
      { id: 'A', text: 'O(1)' },
      { id: 'B', text: 'O(log n)' },
      { id: 'C', text: 'O(n)' },
      { id: 'D', text: 'O(n log n)' },
    ],
    correctAnswer: 'A',
    topic: 'Arrays',
  },
  {
    id: 2,
    questionNumber: 2,
    question: 'Which data structure operates on a Last In, First Out (LIFO) principle?',
    options: [
      { id: 'A', text: 'Queue' },
      { id: 'B', text: 'Stack' },
      { id: 'C', text: 'Linked List' },
      { id: 'D', text: 'Binary Tree' },
    ],
    correctAnswer: 'B',
    topic: 'Stacks',
  },
  {
    id: 3,
    questionNumber: 3,
    question: 'What is the average time complexity of insertion in a singly linked list at the head?',
    options: [
      { id: 'A', text: 'O(n)' },
      { id: 'B', text: 'O(log n)' },
      { id: 'C', text: 'O(1)' },
      { id: 'D', text: 'O(n²)' },
    ],
    correctAnswer: 'C',
    topic: 'Linked Lists',
  },
  {
    id: 4,
    questionNumber: 4,
    question: 'Which of the following sorting algorithms is guaranteed to have O(n log n) worst-case time complexity?',
    options: [
      { id: 'A', text: 'QuickSort' },
      { id: 'B', text: 'MergeSort' },
      { id: 'C', text: 'BubbleSort' },
      { id: 'D', text: 'InsertionSort' },
    ],
    correctAnswer: 'B',
    topic: 'Sorting',
  },
  {
    id: 5,
    questionNumber: 5,
    question: 'In a Hash Table with open addressing, what happens during a collision?',
    options: [
      { id: 'A', text: 'The element is placed into a linked list at that index' },
      { id: 'B', text: 'The hash table expands immediately' },
      { id: 'C', text: 'Alternative slots in the array are probed sequentially' },
      { id: 'D', text: 'The previous element is overwritten' },
    ],
    correctAnswer: 'C',
    topic: 'Hashing',
  },
  {
    id: 6,
    questionNumber: 6,
    question: 'What is the maximum number of children a node in a Binary Search Tree (BST) can have?',
    options: [
      { id: 'A', text: '1' },
      { id: 'B', text: '2' },
      { id: 'C', text: '3' },
      { id: 'D', text: 'Unlimited' },
    ],
    correctAnswer: 'B',
    topic: 'Trees',
  },
  {
    id: 7,
    questionNumber: 7,
    question: 'What is the time complexity of binary search on a sorted array?',
    options: [
      { id: 'A', text: 'O(n)' },
      { id: 'B', text: 'O(log n)' },
      { id: 'C', text: 'O(n²)' },
      { id: 'D', text: 'O(1)' },
    ],
    correctAnswer: 'B',
    topic: 'Binary Search',
  },
  {
    id: 8,
    questionNumber: 8,
    question: 'Which tree traversal visits the root node before visiting its left and right subtrees?',
    options: [
      { id: 'A', text: 'In-order traversal' },
      { id: 'B', text: 'Pre-order traversal' },
      { id: 'C', text: 'Post-order traversal' },
      { id: 'D', text: 'Level-order traversal' },
    ],
    correctAnswer: 'B',
    topic: 'Trees',
  },
  {
    id: 9,
    questionNumber: 9,
    question: 'Which algorithm is commonly used to find the shortest path in a weighted graph with non-negative edge weights?',
    options: [
      { id: 'A', text: "Dijkstra's Algorithm" },
      { id: 'B', text: 'Breadth-First Search (BFS)' },
      { id: 'C', text: 'Depth-First Search (DFS)' },
      { id: 'D', text: "Kruskal's Algorithm" },
    ],
    correctAnswer: 'A',
    topic: 'Graphs',
  },
  {
    id: 10,
    questionNumber: 10,
    question: 'What is the space complexity of Depth-First Search (DFS) on a graph with V vertices and maximum recursion depth D?',
    options: [
      { id: 'A', text: 'O(V²)' },
      { id: 'B', text: 'O(D)' },
      { id: 'C', text: 'O(1)' },
      { id: 'D', text: 'O(V × E)' },
    ],
    correctAnswer: 'B',
    topic: 'Graphs',
  },
  {
    id: 11,
    questionNumber: 11,
    question: 'Which property characterizes a Min-Heap binary tree?',
    options: [
      { id: 'A', text: 'Every parent node is greater than or equal to its children' },
      { id: 'B', text: 'Every parent node is less than or equal to its children' },
      { id: 'C', text: 'Left child is smaller than parent, right child is larger' },
      { id: 'D', text: 'All leaf nodes must be at the same depth' },
    ],
    correctAnswer: 'B',
    topic: 'Heaps',
  },
  {
    id: 12,
    questionNumber: 12,
    question: 'What is the primary condition for applying Dynamic Programming to an algorithmic problem?',
    options: [
      { id: 'A', text: 'Linear data structures with sorted inputs' },
      { id: 'B', text: 'Optimal substructure and overlapping subproblems' },
      { id: 'C', text: 'Unweighted directed graphs' },
      { id: 'D', text: 'Constant time complexity guarantees' },
    ],
    correctAnswer: 'B',
    topic: 'Dynamic Programming',
  },
  {
    id: 13,
    questionNumber: 13,
    question: 'What is the worst-case time complexity of searching in an unbalanced Binary Search Tree of size n?',
    options: [
      { id: 'A', text: 'O(1)' },
      { id: 'B', text: 'O(log n)' },
      { id: 'C', text: 'O(n)' },
      { id: 'D', text: 'O(n log n)' },
    ],
    correctAnswer: 'C',
    topic: 'Trees',
  },
  {
    id: 14,
    questionNumber: 14,
    question: 'In a circular queue implemented using an array of size N, how is the next index calculated?',
    options: [
      { id: 'A', text: '(index + 1)' },
      { id: 'B', text: '(index + 1) % N' },
      { id: 'C', text: '(index - 1) % N' },
      { id: 'D', text: 'index * 2' },
    ],
    correctAnswer: 'B',
    topic: 'Queues',
  },
  {
    id: 15,
    questionNumber: 15,
    question: 'Which technique is used by QuickSelect to find the k-th smallest element in an unsorted array?',
    options: [
      { id: 'A', text: 'Partitioning around a pivot element' },
      { id: 'B', text: 'Building a Min-Heap of all elements' },
      { id: 'C', text: 'Bitwise manipulation' },
      { id: 'D', text: 'Topological sorting' },
    ],
    correctAnswer: 'A',
    topic: 'Algorithms',
  },
  {
    id: 16,
    questionNumber: 16,
    question: 'What data structure is typically used to implement Breadth-First Search (BFS)?',
    options: [
      { id: 'A', text: 'Stack' },
      { id: 'B', text: 'Queue' },
      { id: 'C', text: 'Priority Queue' },
      { id: 'D', text: 'Binary Search Tree' },
    ],
    correctAnswer: 'B',
    topic: 'Graphs',
  },
  {
    id: 17,
    questionNumber: 17,
    question: 'What is the time complexity of finding an element in an ideal Hash Map with zero collisions?',
    options: [
      { id: 'A', text: 'O(1)' },
      { id: 'B', text: 'O(log n)' },
      { id: 'C', text: 'O(n)' },
      { id: 'D', text: 'O(n²)' },
    ],
    correctAnswer: 'A',
    topic: 'Hashing',
  },
  {
    id: 18,
    questionNumber: 18,
    question: 'Which algorithmic paradigm does the MergeSort algorithm follow?',
    options: [
      { id: 'A', text: 'Greedy' },
      { id: 'B', text: 'Dynamic Programming' },
      { id: 'C', text: 'Divide and Conquer' },
      { id: 'D', text: 'Backtracking' },
    ],
    correctAnswer: 'C',
    topic: 'Sorting',
  },
  {
    id: 19,
    questionNumber: 19,
    question: 'How do you detect a cycle in a singly linked list in O(n) time and O(1) space?',
    options: [
      { id: 'A', text: 'Using a Hash Set of visited nodes' },
      { id: 'B', text: "Floyd's Tortoise and Hare two-pointer algorithm" },
      { id: 'C', text: 'Recursively reversing the list' },
      { id: 'D', text: 'Counting the length of the list twice' },
    ],
    correctAnswer: 'B',
    topic: 'Linked Lists',
  },
  {
    id: 20,
    questionNumber: 20,
    question: 'What is the amortized time complexity of appending an element to a dynamic array (like ArrayList or vector)?',
    options: [
      { id: 'A', text: 'O(1)' },
      { id: 'B', text: 'O(log n)' },
      { id: 'C', text: 'O(n)' },
      { id: 'D', text: 'O(n log n)' },
    ],
    correctAnswer: 'A',
    topic: 'Arrays',
  },
];
