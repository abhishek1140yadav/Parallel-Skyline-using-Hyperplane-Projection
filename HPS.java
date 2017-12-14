import java.io.File;
import java.util.Scanner;
import java.util.Random;
import java.util.Comparator;
import java.util.Arrays;
class HPS implements Runnable
{
 private static double data[][],avg[];
 private static int dim;
 private int start,end;

 private HPS(int start,int end)
 {
  this.start=start;this.end=end;
 }

 public void run()
 {
  int i,j;
  double sum;
  for(i=start;i<=end;i++)
  {
   sum=0;
   for(j=1;j<=dim;j++)sum+=data[i][j]/avg[j];
   for(j=1;j<=dim;j++)data[i][dim+j]=data[i][j]/(sum*avg[j]);
  }
 }

 private static class SplitMerge implements Runnable
 {
  int start,end,cores,skysize,d;
  double skylines[][];

  SplitMerge(int start,int end,int d,int cores)
  {
   this.start=start;this.end=end;this.d=d;this.cores=cores;
  }

  private int partition(int first,int last)
  {
   int pivot=first+new Random().nextInt(last-first+1),i;
   double tm[]=data[last],p;
   data[last]=data[pivot];
   data[pivot]=tm;
   p=data[last][dim+1+d];
   for(i=first;i<last;i++)
    if(data[i][dim+1+d]<p)
    {
     tm=data[i];data[i]=data[first];data[first]=tm;
     first++;
    }
   tm=data[first];data[first]=data[last];data[last]=tm;
   return first;
  }

  private void quickselect(int first,int last,int k)
  {
   int pivot=partition(first,last);
   if(pivot==k)return;
   if(pivot>k)quickselect(first,pivot-1,k);
   else quickselect(pivot+1,last,k);
  }

  boolean dominate(double a[],double b[])
  {
   int i;
   for(i=1;i<=dim;i++)if(a[i]>b[i])return false;
   return true;
  }

  public void computeSkylines()
  {
   Arrays.sort(data,start,end,new Comparator<double[]>(){public int compare(double a[],double b[]){return Double.compare(a[2*dim+1],b[2*dim+1]);}});
   for(int i=start;i<=end;i++){int j;for(j=start;j<=end;j++)if(i!=j&&dominate(data[j],data[i]))break;if(j==end+1)skylines[skysize++]=data[i];}
  }

  public void merge(double sky1[][],int size1,double sky2[][],int size2)
  {
   int i,j;
   for(i=0;i<size1;i++){for(j=0;j<size2;j++)if(dominate(sky2[j],sky1[i]))break;if(j==size2)skylines[skysize++]=sky1[i];}
   for(i=0;i<size2;i++){for(j=0;j<size1;j++)if(dominate(sky1[j],sky2[i]))break;if(j==size1)skylines[skysize++]=sky2[i];}
  }

  public void run()
  {
   int n1,n2,splitIndex;
   skylines=new double[end-start+1][];
   skysize=0;
   if(cores==1)
   {
    computeSkylines();
    return;
   }
   n1=cores/2;
   n2=cores-n1;
   splitIndex=start+(end-start+1)*n1/cores;
   quickselect(start,end,splitIndex);
   SplitMerge sm1=new SplitMerge(start,splitIndex-1,(d+1)%dim,n1),sm2=new SplitMerge(splitIndex,end,(d+1)%dim,n2);
   Thread t1=new Thread(sm1),t2=new Thread(sm2);
   t1.start();t2.start();
   try{t1.join();t2.join();}catch(Exception e){System.out.println("Problem Joining threads");System.exit(0);}
   merge(sm1.skylines,sm1.skysize,sm2.skylines,sm2.skysize);
  }
 }

 public static void main(String args[]) throws Exception
 {
  Thread t[];
  int i,j,no_obj,cores;
  long startTime = System.currentTimeMillis(),time;
  File file=new File("DATA.txt");
  Scanner scan;
  if(args.length<2){System.out.println("Usage: java HPS <no_obj> <no_dim> <file-name> <no_threads_to_create>");return;}
  try
  {
   no_obj=Integer.parseInt(args[0]);
   dim=Integer.parseInt(args[1]);
   if(args.length>=4)cores=Integer.parseInt(args[3]);else cores=Runtime.getRuntime().availableProcessors();
  }
  catch(Exception e){System.out.println("Usage: java HPS <no_obj> <no_dim> <file-name> <no_threads_to_create>");return;}
  if(args.length>=3)
   file=new File(args[2]);
  try{scan=new Scanner(file);}catch(Exception e){System.out.println("File: "+file.getName()+" not found.");return;}

  double temp,sum;
  data=new double[no_obj][2*dim+2];
  avg=new double[dim+1];
  t=new Thread[cores];
  for(i=0;i<dim+1;i++)avg[i]=0;

  //Scanning data ,and avg and entropy is also calculated
  for(i=0;i<no_obj;i++)
  {
   sum=0;
   for(j=0;j<dim+1;j++)
   {
    temp=scan.nextDouble();
    avg[j]+=temp;
    data[i][j]=temp;
    sum+=Math.log(temp+1);
   }
   data[i][2*dim+1]=sum;
  }
  scan.close();
  for(i=0;i<dim+1;i++)avg[i]/=no_obj;

  time=System.currentTimeMillis();
  System.out.println("Time taken to read from file : "+(time-startTime)+" ms");
  startTime=time;

  //Data split and given to different thread to project it to the hyperplane d1+d2+...+dn=1
  for(i=0;i<cores;i++)
  {
   t[i]=new Thread(new HPS(i*no_obj/cores,(i+1)*no_obj/cores-1));
   t[i].start();
  }
  //Waiting for each thread to finish projecting
  for(i=0;i<cores;i++)t[i].join();

  SplitMerge sm=new SplitMerge(0,no_obj-1,0,cores);
  t[0]=new Thread(sm);
  t[0].start();
  t[0].join();
  time   = System.currentTimeMillis();
  System.out.println("Time taken by the algorithm : "+(time-startTime)+"ms\nSkyline set size : "+sm.skysize);
/*
//To show data
  for(i=0;i<sm.skysize;i++)
  {
   for(j=0;j<2*dim+1;j++)System.out.print(sm.skylines[i][j]+"\t");
   System.out.println();
  }
/**/
 }
}

