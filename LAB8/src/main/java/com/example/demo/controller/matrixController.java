package com.example.demo.controller;
import com.example.demo.dto.MatrixRequest;
import com.example.demo.service.Fox;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matrix")
public class matrixController {

    @PostMapping("/multiply")
    public int[][] multiply(@RequestBody MatrixRequest request) {
        Fox.Result result = Fox.foxAlgorithmParallel(
                request.getMatrix1(),
                request.getMatrix2(),
                request.getThreads()
        );
        return result.getResultMatrix();
    }

    @GetMapping("/generate")
    public int[][] generateAndMultiply(
            @RequestParam(defaultValue = "200") int size,
            @RequestParam(defaultValue = "4") int threads
    ) {
        int[][] m1 = generateMatrix(size);
        int[][] m2 = generateMatrix(size);
        Fox.Result result = Fox.foxAlgorithmParallel(m1, m2, threads);
        return result.getResultMatrix();
    }

    private int[][] generateMatrix(int size) {
        int[][] m = new int[size][size];
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                m[i][j] = (int)(Math.random() * 10);
        return m;
    }
}
