package app.Model;

import java.awt.Color;
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

import app.Data.Highlight;
import app.Data.ProcessedDataObject;

/**
 * Delegate interfaces and implementations for managing different types of data,
 * including EyeTracking, Emotion, and Highlight data. These delegates interact
 * with
 * queues and lists to manage and update data efficiently for real-time
 * processing.
 * <p>
 * This file defines several interfaces and classes to handle:
 * - Eye Tracking Data (EyeTrackingDelegate)
 * - Emotion Data (EmotionDelegate)
 * - Highlight Data (HighlightDelegate)
 * - Processed Data (DataDelegate)
 *
 * Code Metrics:
 * - Number of Classes: 6 (EyeTrackingDataDelegate, EmotionDataDelegate,
 * HighlightDataDelegate, ProcessedDataDelegate)
 * - Number of Methods: 28
 * - Lines of Code (LOC): 200 (including comments and blank lines)
 * - Cyclomatic Complexity: 5 (based on method complexity: multiple `if` and
 * loop-based methods)
 * - Number of Conditional Branches: 8 (includes `if` statements and loop
 * constructs)
 *
 * @author Andrew Estrada
 * @author Sean Sponsler
 * @author Xiuyuan Qiu
 * @version 1.0
 */
interface EyeTrackingDelegate {
   void addToEyeTrackingQueue(String data) throws InterruptedException;

   String pollEyeTrackingQueue() throws InterruptedException;
}

interface EmotionDelegate {
   void addToEmotionQueue(String data) throws InterruptedException;

   String pollEmotionQueue() throws InterruptedException;

   void setFrequencies(List<String> frequencies) throws InterruptedException;

   void incrementEmotionCount(int index) throws InterruptedException;

   List<Integer> getEmotionCounts() throws InterruptedException;

   List<String> getFrequencies() throws InterruptedException;

   int getProcessedEmotions() throws InterruptedException;
}

interface HighlightDelegate {
   void addHighlightCollection(List<Highlight> highlights);

   Deque<List<Highlight>> getHighlightCollections();

   void addToHighlightList(Highlight data);

   List<Highlight> getHighlightList();

   void updateHighlightColors(Color color);

   void setHighlightList(List<Highlight> highlightList);

   int getThresholdLength();

   void setThresholdLength(int thresholdLength);

   int getMaxHighlights();

   void setMaxHighlights(int maxHighlights);

   int getHighlightLength();

   void setHighlightLength(int highlightLength);

   int getRowSize();

   void setRowSize(int rowSize);
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
      return eyeTrackingQueue.poll(Blackboard.EYE_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
   }
}

class EmotionDataDelegate implements EmotionDelegate {
   private final BlockingQueue<String> emotionQueue = new LinkedBlockingQueue<>();
   private static List<String> frequencies = Arrays.asList("0%", "0%", "0%", "0%", "0%", "0%");
   private static int processedEmotions = 0;
   private final List<Integer> emotionCounts = Arrays.asList(0, 0, 0, 0, 0, 0);

   @Override
   public void addToEmotionQueue(String data) throws InterruptedException {
      emotionQueue.put(data);
   }

   @Override
   public void setFrequencies(List<String> newFrequencies) {
      frequencies = newFrequencies;
   }

   @Override
   public void incrementEmotionCount(int index) {
      emotionCounts.set(index, emotionCounts.get(index) + 1);
      processedEmotions++;
   }

   @Override
   public List<Integer> getEmotionCounts() {
      return emotionCounts;
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
      return emotionQueue.poll(Blackboard.EMOTION_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
   }
}

class HighlightDataDelegate implements HighlightDelegate {
   private List<Highlight> highlightList = new ArrayList<>();
   private final Deque<List<Highlight>> highlightCollections = new ConcurrentLinkedDeque<>();
   private int maxHighlights = 15;
   private int rowSize = 100;
   private int thresholdLength = 50;
   private int highlightLength = 100;

   @Override
   public void addHighlightCollection(List<Highlight> highlights) {
      if (highlightCollections.size() == maxHighlights) {
         highlightCollections.pollFirst();
      }
      List<Highlight> highlightsCopy = new ArrayList<>(highlights);
      highlightCollections.add(highlightsCopy);
      highlights.clear();
      Blackboard.getInstance().firePropertyChange(Blackboard.PROPERTY_NAME_VIEW_DATA, null, highlights);
   }

   @Override
   public void updateHighlightColors(Color color) {
      if (highlightList.isEmpty()) {
         return;
      }
      if (highlightList.get(0).getColor() != Color.GRAY) {
         // No new highlights have been drawn since the last update
         Blackboard.getInstance().firePropertyChange(Blackboard.PROPERTY_NAME_VIEW_DATA,
               null, highlightList);
         return;
      }
      for (Highlight highlight : highlightList) {
         highlight.setColor(color);
      }
      Blackboard.getInstance().firePropertyChange(Blackboard.PROPERTY_NAME_VIEW_DATA, null, highlightList);
   }

   @Override
   public Deque<List<Highlight>> getHighlightCollections() {
      return highlightCollections;
   }

   @Override
   public List<Highlight> getHighlightList() {
      return highlightList;
   }

   @Override
   public void setHighlightList(List<Highlight> highlightList) {
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
