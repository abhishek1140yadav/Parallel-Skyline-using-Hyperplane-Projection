import java.io.File;
import java.util.Scanner;
import java.util.Comparator;
import java.util.Arrays;
class SFS
{
 private static int dim;
 private static boolean dominate(double a[],double b[])
 {
  int i;
  for(i=1;i<=dim;i++)if(a[i]>b[i])return false;
  return true;
 }

 public static void main(String args[]) throws Exception
 {
  int i,j,no_obj,skysize=0;
  double skylines[][],data[][]; 
  long startTime = System.currentTimeMillis(),time;
  double sum,temp;
  File file=new File("DATA.txt");
  Scanner scan;
  if(args.length<2){System.out.println("Usage: java SFS <no_obj> <no_dim> <file-name>");return;}
  try
  {
   no_obj=Integer.parseInt(args[0]);
   dim=Integer.parseInt(args[1]);
  }
  catch(Exception e){System.out.println("Usage: java SFS <no_obj> <no_dim> <file-name>");return;}
  if(args.length>=3)
   file=new File(args[2]);
  try{scan=new Scanner(file);}catch(Exception e){System.out.println("File: "+file.getName()+" not found.");return;}

  data=new double[no_obj][dim+2];

  //Scanning data ,and avg and entropy is also calculated
  for(i=0;i<no_obj;i++)
  {
   sum=0;
   for(j=0;j<dim+1;j++)
   {
    temp=scan.nextDouble();
    data[i][j]=temp;
    sum+=Math.log(temp+1);
   }
   data[i][dim+1]=sum;
  }
  scan.close();

  time=System.currentTimeMillis();
  System.out.println("Time taken to read from file : "+(time-startTime)+" ms");
  startTime=time;

  skylines=new double[no_obj][];
  Arrays.sort(data,0,no_obj-1,new Comparator<double[]>(){public int compare(double a[],double b[]){return Double.compare(a[dim+1],b[dim+1]);}});
  for(i=0;i<=no_obj-1;i++)
  {
   for(j=0;j<=no_obj-1;j++)
    if(i!=j&&dominate(data[j],data[i]))
     break;
   if(j==no_obj)skylines[skysize++]=data[i];
  }

  time   = System.currentTimeMillis();
  System.out.println("Time taken by the algorithm : "+(time-startTime)+"ms\nSkyline set size : "+skysize);
/*
//To show data
  for(i=0;i<skysize;i++)
  {
   for(j=0;j<dim+1;j++)System.out.print(skylines[i][j]+"\t");
   System.out.println();
  }
/**/
 }
}

