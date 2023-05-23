void parallel_openMP(int *graph, int *dist, int *visited) {    dist[0] = 0;    for (int i = 0; i < VERTICES; i++) {
        int min_dist = INF + 1; //所有未访问节点中的最短距离
        int v = -1; //所有未访问节点中的最近节点
        //每个线程的私有变量，记录当前线程中的最短距离和最近节点
        int thread_min_dist = min_dist;
        int thread_v = v;
#pragma omp parallel  firstprivate(thread_min_dist, thread_v)
        {   #pragma omp for nowait
            for (int j = 0; j < VERTICES; j++) {
                if ((dist[j] < thread_min_dist) && (visited[j] == 0)) {
                    thread_min_dist = dist[j];
                    thread_v = j;
                }
            }
#pragma omp critical
            {
                if (thread_min_dist < min_dist) {
                    min_dist = thread_min_dist;
                    v = thread_v;
                }
            }
        }
        visited[v] = 1;
        if (dist[v] == INF)continue;