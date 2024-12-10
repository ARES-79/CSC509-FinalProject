package app.Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import app.Data.Circle;
import app.Data.Highlight;
import app.Data.ProcessedDataObject;

interface EyeTrackingDelegate {
    void addToEyeTrackingQueue(String data) throws InterruptedException;
    String pollEyeTrackingQueue() throws InterruptedException;
}

interface EmotionDelegate {
    void addToEmotionQueue(String data) throws InterruptedException;
    String pollEmotionQueue() throws InterruptedException;
    public void setFrequencies(List<String> frequencies) throws InterruptedException;
    public void incrementEmotions() throws InterruptedException;
    public List<String> getFrequencies() throws InterruptedException;
    public int getProcessedEmotions() throws InterruptedException;
}

interface HighlightDelegate {
      void addToHighlightList(Highlight data);
      public Deque<Highlight> getHighlightList();
      public void setHighlightList(Deque<Highlight> highlightList);
      public int getThresholdLength();
      public void setThresholdLength(int thresholdLength);
      public int getMaxHighlights();
      public void setMaxHighlights(int maxHighlights);
      public int getHighlightLength();
      public void setHighlightLength(int highlightLength);
      public int getRowSize();
      public void setRowSize(int rowSize);
}

interface CircleDelegate {
      void addToCircleList(Circle data);
      public Deque<Circle> getCircleList();
      public void setCircleList(Deque<Circle> circleList);
      public int getThresholdRadius();
      public int getCircleRadius();
      public void setThresholdRadius(int thresholdRadius);
      public void setCircleRadius(int circleRadius);
      public int getMaxCircles();
      public void setMaxCircles(int maxCircles);
}

interface DataDelegate {
    void addToProcessedDataQueue(ProcessedDataObject data);
    ProcessedDataObject getFromProcessedDataQueue();
}

class EyeTrackingDataDelegate implements EyeTrackingDelegate {
    private final BlockingQueue<String> eyeTrackingQueue = new LinkedBlockingQueue<>();

    @Override
    public void addToEyeTrackingQueue(String data) throws InterruptedException {
        eyeTrackingQueue.put(data);
    }

    @Override
    public String pollEyeTrackingQueue() throws InterruptedException {
        return eyeTrackingQueue.poll(Blackboard.TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
    }
}

class EmotionDataDelegate implements EmotionDelegate {
    private final BlockingQueue<String> emotionQueue = new LinkedBlockingQueue<>();
    private static ArrayList<List<Float>> emotionScores = new ArrayList<>();
    private static List<String> frequencies = Arrays.asList("0%", "0%", "0%", "0%", "0%");
    private static int processedEmotions = 0;

    @Override
    public void addToEmotionQueue(String data) throws InterruptedException {
        emotionQueue.put(data);
    }

    @Override
    public void setFrequencies(List<String> frequencies) {
       this.frequencies = frequencies;
    }

    @Override
    public void incrementEmotions() {
        this.processedEmotions++;
    }

    @Override
    public List<String> getFrequencies() throws InterruptedException {
        return frequencies;
    }

    @Override
    public int getProcessedEmotions() throws InterruptedException {
        return processedEmotions;
    }

    @Override
    public String pollEmotionQueue() throws InterruptedException {
        return emotionQueue.poll(Blackboard.TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
    }

}

class HighlightDataDelegate implements HighlightDelegate {
   private Deque<Highlight> highlightList = new ConcurrentLinkedDeque<>();
   private int maxHighlights = 10;
   private int rowSize = 100;
   private int thresholdLength = 100;
   private int highlightLength = 100;

   @Override
   public Deque<Highlight> getHighlightList() {
      return highlightList;
   }

   @Override
   public void setHighlightList(Deque<Highlight> highlightList) {
      this.highlightList = highlightList;
      Blackboard.getInstance().firePropertyChange(Blackboard.PROPERTY_NAME_VIEW_DATA, null, highlightList);
   }

   @Override
   public void addToHighlightList(Highlight data) {
      highlightList.add(data);
      Blackboard.getInstance().firePropertyChange(Blackboard.PROPERTY_NAME_VIEW_DATA, null, null);
   }

   @Override
   public int getThresholdLength() {
      return thresholdLength;
   }

   @Override
   public void setThresholdLength(int thresholdLength) {
      this.thresholdLength = thresholdLength;
   }

   @Override
   public int getMaxHighlights() {
      return maxHighlights;
   }

   @Override
   public void setMaxHighlights(int maxHighlights) {
      this.maxHighlights = maxHighlights;
   }

   @Override
   public int getHighlightLength() {
      return highlightLength;
   }

   @Override
   public void setHighlightLength(int highlightLength) {
      this.highlightLength = highlightLength;
   }

   @Override
   public int getRowSize() {
      return rowSize;
   }

   @Override
   public void setRowSize(int rowSize) {
      this.rowSize = rowSize;
   }

}

class CircleDataDelegate implements CircleDelegate {
    private Deque<Circle> circleList = new ConcurrentLinkedDeque<>();
    private int maxCircles = 5;
    private int thresholdRadius = 50;
    private int circleRadius = 50;

    @Override
    public void addToCircleList(Circle data) {
        circleList.add(data);
        Blackboard.getInstance().firePropertyChange(Blackboard.PROPERTY_NAME_VIEW_DATA, null, null);
    }

    @Override
      public Deque<Circle> getCircleList() {
         return circleList;
    }

    @Override
    public void setCircleList(Deque<Circle> circleList) {
       this.circleList = circleList;
       Blackboard.getInstance().firePropertyChange(Blackboard.PROPERTY_NAME_VIEW_DATA, null, circleList);
    }

   @Override
   public int getThresholdRadius() {
      return thresholdRadius;
   }
   @Override
   public int getCircleRadius() {
      return circleRadius;
   }
   @Override
   public void setThresholdRadius(int thresholdRadius) {
      this.thresholdRadius = thresholdRadius;
   }
   @Override
   public void setCircleRadius(int circleRadius) {
      this.circleRadius = circleRadius;
   }
   @Override 
   public int getMaxCircles() {
      return maxCircles;
   }
   @Override
   public void setMaxCircles(int maxCircles) {
      this.maxCircles = maxCircles;
   }
}

class ProcessedDataDelegate implements DataDelegate {
    private final Queue<ProcessedDataObject> processedDataQueue = new ConcurrentLinkedQueue<>();

    @Override
    public void addToProcessedDataQueue(ProcessedDataObject data) {
        processedDataQueue.add(data);
        Blackboard.getInstance().firePropertyChange(Blackboard.PROPERTY_NAME_PROCESSED_DATA, null, null);
    }

    @Override
    public ProcessedDataObject getFromProcessedDataQueue() {
        return processedDataQueue.poll();
    }
}
