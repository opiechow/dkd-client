package oskpiech.pg.edu.pl.dormkeydispenser;

class MovingAverage {
    private int size;
    private int currentLength;
    private int nextSampleIdx;
    private double total;
    private double[] samples;

    MovingAverage(int size) {
        this.nextSampleIdx = 0;
        this.currentLength = 0;
        this.total = 0;
        this.size = size;
        this.samples = new double[size];
        for (int i = 0; i < size; i++) {
            this.samples[i] = 0d;
        }
    }

    void addSample(double sample) {
        if (currentLength == size) {
            total -= samples[nextSampleIdx];
            /* Adjust for adding later */
            currentLength -= 1;
        }
        samples[nextSampleIdx] = sample;
        total += sample;
        nextSampleIdx = (nextSampleIdx + 1) % size;
        currentLength += 1;
    }

    double getAverage() {
        if (currentLength > 0) {
            return total / currentLength;
        }
        return 0;
    }
}
