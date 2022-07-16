import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Main {
    public static void main(String[] args) {
		
        Scanner sc = new Scanner(System.in);
        long N = sc.nextLong(); // N - number of spending;
        int D = sc.nextInt(); sc.nextLine(); // D - number of trailing days
        
		ArrayList<Integer> ListOfRef = new ArrayList<>(); // used for storing references (optimisation)
        ArrayList<Purchase> ListOfPurchases = new ArrayList<>();
        
		int[] IndexesOfDates = new int[999999]; // key - date, value - index;

        for (long i = 0; i < N; i++) {
            String CurrentInput = sc.nextLine(); // input example: 2022-01-14 $30.00

            int CurrentKey = algorithms.GetKey(CurrentInput);
            double CurrentValue = algorithms.GetValue(CurrentInput);

            Purchase p = new Purchase(CurrentKey);
            // if our list already contains CurrentKey (Date), we add a new spending to it,
            // otherwise we put completed Purchase p into our list
            if (IndexesOfDates[CurrentKey] != 0)
                ListOfPurchases.get(IndexesOfDates[CurrentKey] - 1).Spending.add(CurrentValue);
            else {
                p.Spending.add(CurrentValue);
                ListOfPurchases.add(p);
                IndexesOfDates[CurrentKey] = ListOfPurchases.size();
            }
        }
		
		//adding completed purchaces to reference list for optimisation
        for (int i = 0; i < ListOfPurchases.size(); i++)
            ListOfRef.add(i);
		
		// sorting the list by dates
        algorithms.SortList(ListOfPurchases, 0, ListOfPurchases.size() - 1, ListOfRef);

        int FinalRes = 0, FirstIndex = ListOfPurchases.get(0).date, CurrentDate = 0, PrevDate;
        double CurrentSum, CurrentMedian = -1; // -1 is used for yet undefined median
        double[] Medians = new double[D];

        for (int i = 0; i < ListOfPurchases.size(); i++) {
            CurrentSum = 0;
            PrevDate = CurrentDate;
            CurrentDate = ListOfPurchases.get(i).date - FirstIndex;

            // Medians update
            if (CurrentDate - PrevDate > D) {
                algorithms.ClearArray(Medians);
                CurrentMedian = 0.0;
            }
            else {
                if (CurrentDate >= D) { // if CurrentDate < D, then CurrentMedian should be undefined
                    for (int j = (PrevDate + 1) % D; j < CurrentDate - PrevDate - 1 + (PrevDate + 1) % D; j++)
                        Medians[j % D] = 0.0;
                    double[] temp = new double[D];
                    System.arraycopy(Medians, 0, temp, 0, D);
                    CurrentMedian = algorithms.FindMedian(temp);
                }
            }

            for (int j = 0; j < ListOfPurchases.get((ListOfRef.get(i))).Spending.size(); j++) {
                CurrentSum += ListOfPurchases.get(ListOfRef.get(i)).Spending.get(j);
                if (CurrentSum >= 2 * CurrentMedian && CurrentMedian > -1)
                    FinalRes++;
            }

            Medians[CurrentDate % D] = CurrentSum;
        }

        System.out.println(FinalRes);
    }
	
	// Class defining the Purchase, contains a reference to date and the spending value
    public static class Purchase implements Comparable<Purchase>{
        public int date;
        ArrayList<Double> Spending;

        public Purchase(int date) {
            this.date = date;
            Spending = new ArrayList<>();
        }

        @Override
        public boolean equals(Object purchase) {
            if (purchase == this) {
                return true;
            }
            if (!(purchase instanceof Purchase)) {
                return false;
            }
            Purchase p = (Purchase) purchase;
            return this.date == p.date;
        }
        @Override
        public int compareTo(Purchase o) {
            return Integer.compare(this.date, o.date);
        }
        public void Assign(Purchase o) {
            this.date = o.date;
            this.Spending.clear();
            this.Spending.addAll(o.Spending);
        }

        public void PrintPurchase() {
            System.out.print("Date: " + date + ": " + Spending.get(0));
            for (long i = 1; i < Spending.size(); i++)
                System.out.print(", " + Spending.get(1));
            System.out.print("\n");
        }
        public Purchase copy() {
            Purchase newPurchase = new Purchase(this.date);
            newPurchase.Spending.addAll(this.Spending);
            return newPurchase;
        }
    }
	
	// Helper class that has useful algorithms
    public static class algorithms {
		
		// transform date into int
        static int GetKey(String CurrentInput) {
            LocalDate start = LocalDate.of(1000,1,1);
            LocalDate now = LocalDate.of(Integer.parseInt(CurrentInput.substring(0,4)), Integer.parseInt(CurrentInput.substring(5,7)),
                    Integer.parseInt(CurrentInput.substring(8,10)));
            return (int) ChronoUnit.DAYS.between(start,now);
        }
		
        static double GetValue(String CurrentInput) {
            return Double.parseDouble(CurrentInput.substring(12));
        }
		
        public static void ClearArray(double[] Arr) {
            Arrays.fill(Arr, 0.0);
        }
		
        public static double FindMax (double[] arr) {
            double res = arr[0];
            for (double el: arr) if (el > res) res = el;
            return res;
        }
		
        public static double FindMin (double[] arr) {
            double res = arr[0];
            for (double el: arr) if (el < res) res = el;
            return res;
        }

        // using comparison-based Merge Sort Algorithm
        public static void SortList(ArrayList<Purchase> ListOfPurchases, int begin, int end, ArrayList<Integer> ListOfRef) {
            if (begin < end) {
                int middle = begin + (end - begin) / 2;
                SortList(ListOfPurchases, begin, middle, ListOfRef);
                SortList(ListOfPurchases, middle + 1, end, ListOfRef);
                Merge(ListOfPurchases, begin, middle, end, ListOfRef);
            }
        }
        private static void Merge(ArrayList<Purchase> ListOfPurchases, int begin, int middle, int end, ArrayList<Integer> ListOfRef) {
            int size1 = middle - begin + 1;
            int size2 = end - middle;

            int[] arr1 = new int[size1];
            int[] arr2 = new int[size2];
            int[] arr1ref = new int[size1];
            int[] arr2ref = new int[size2];

            for (int i = 0; i < size1; i++) {
                arr1[i] = ListOfPurchases.get(begin + i).date;
                arr1ref[i] = ListOfRef.get(begin + i);
            }
            for (int i = 0; i < size2; i++) {
                arr2[i] = ListOfPurchases.get(middle + 1 + i).date;
                arr2ref[i] = ListOfRef.get(middle + 1 + i);
            }

            int i = 0, j = 0, k = begin;
            while (i < size1 && j < size2) {
                if (arr1[i] <= arr2[j]) {
                    ListOfPurchases.get(k).date = arr1[i];
                    ListOfRef.set(k, arr1ref[i]);
                    i++;
                }
                else {
                    ListOfPurchases.get(k).date = arr2[j];
                    ListOfRef.set(k, arr2ref[j]);
                    j++;
                }
                k++;
            }
            while (i < size1) {
                ListOfPurchases.get(k).date = arr1[i];
                ListOfRef.set(k, arr1ref[i]);
                i++;
                k++;
            }
            while (j < size2) {
                ListOfPurchases.get(k).date = arr2[j];
                ListOfRef.set(k, arr2ref[j]);
                j++;
                k++;
            }
        }

        // using linear-based Bucket Sort Algorithm
        public static void MedianSort(double[] arr) {
            int nBuckets = arr.length / 3;
            double maxElement = FindMax(arr);
            double minElement = FindMin(arr);

            double range = (maxElement - minElement) / nBuckets;
            @SuppressWarnings("unchecked")
            Vector<Double>[] temp = new Vector[nBuckets];
            for (int i = 0; i < nBuckets; i++)
                temp[i] = new Vector<>();

            for (double v : arr) {
                double diff = (v - minElement) / range - (long) ((v - minElement) / range);

                if ((diff != 0 || v == minElement) && (int)((v - minElement) / range) < temp.length)
                    temp[(int) ((v - minElement) / range)].add(v);
                else
                    temp[(int) ((v - minElement) / range) - 1].add(v);
            }

            for (int i = 0; i < nBuckets; i++) if (temp[i].size() != 0) Collections.sort(temp[i]);

            int idx = 0;
            for (int i = 0; i < nBuckets; i++) {
                for (int j = 0; j < temp[i].size(); j++) {
                    arr[idx] = temp[i].get(j);
                    idx++;
                }
            }
        }

        public static double FindMedian(double[] arr) {
            MedianSort(arr);
            if (arr.length % 2 == 1)
                return arr[arr.length / 2];
            else
                return (arr[arr.length / 2 - 1] + arr[arr.length / 2]) / 2; // average
        }
    }
}