package uk.ac.imperial.doc.jexpressions.javaoutput.utils;

import org.apache.commons.math3.analysis.solvers.LaguerreSolver;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

/* An attempt to port the distribution estimation code in http://webspn.hit.bme.hu/~telek/tools.htm */

public class DistributionEstimation {

private static int OK = 1;
private static int NO = 0;
private static double INFTY = Double.MAX_VALUE;

	public static double[] estimateDistribution(int NMoments, double Moments[], double x, double a, double b)
	{
		double Ucdf; double Lcdf; double Uccdf; double Lccdf;			
		double[][] Bounds1 = new double[21][4]; 
		double[][] Bounds2= new double[21][4]; 
		double[][] Bounds3= new double[21][4]; 

		double[] mom = new double[21];
		double[] pos = new double[10];
		double[] weight = new double[10];

		double p;
		double max_pos,min_pos,sum;

		int i,n;

		if(NMoments<2||Moments[0]!=1) 
			{Ucdf = 1.0;
			 Uccdf=1.0; Lcdf = 0.0; Lccdf=0.0; return new double[]{Ucdf, Lcdf, Uccdf, Lccdf};}
		if(NMoments>21) NMoments=21;

		if(x<a) {Ucdf=0.0; Uccdf=1.0; Lcdf=0.0; Lccdf=1.0; return new double[]{Ucdf, Lcdf, Uccdf, Lccdf};}
		if(x>b) {Ucdf=1.0; Uccdf=0.0; Lcdf=1.0; Lccdf=0.0; return new double[]{Ucdf, Lcdf, Uccdf, Lccdf};}

		for(n=0;n<NMoments;n++)
			{Bounds1[n][0]=1.0;
			Bounds2[n][0]=1.0;
			Bounds3[n][0]=1.0;
		 	 Bounds1[n][1]=0.0;
		 	 Bounds2[n][1]=0.0;
		 	 Bounds3[n][1]=0.0;
			 Bounds1[n][2]=1.0;
			 Bounds2[n][2]=1.0;
			 Bounds3[n][2]=1.0;
			 Bounds1[n][3]=0.0;
			 Bounds2[n][3]=0.0;
			 Bounds3[n][3]=0.0;
			}

		MoveMoments(NMoments,Moments,mom,1.0,-x);
		for(n=3;n<=NMoments;n=n+2)
			{ mom[0]=1.0;
			  if(CheckMoments1(n,mom)!=OK) continue;
			  p = Mass1(n,mom);
			  if(p<=0||p>=1) continue;
			  mom[0]=1.0-p;
			  DiscreteConstruction(n,mom,pos,weight);
				
			  max_pos=-INFTY;
			  min_pos=INFTY;
			  for(i=0;i<=(n-1)/2-1;i++) 
				{max_pos=Math.max(max_pos,pos[i]); min_pos=Math.min(min_pos,pos[i]);}

			  if(max_pos<0)
				{ 
				  Bounds1[n-1][0]=1.0; Bounds1[n-1][1]=1.0-p; 
				  Bounds1[n-1][2]=p;   Bounds1[n-1][3]=0; 
				}
			  else if(min_pos>0)
				{
				  Bounds1[n-1][0]=p;   Bounds1[n-1][1]=0; 
				  Bounds1[n-1][2]=1;   Bounds1[n-1][3]=1.0-p; 
				}
			  else
				{ sum=0;
				  for(i=0;i<=(n-1)/2-1;i++)
					{sum=sum+weight[i];
					 if(weight[i]<0) sum = -INFTY;
					}
				  if(Math.abs(sum+p-1.0)>1e-10) continue;
				  	
				  Bounds1[n-1][1]=Bounds1[n-1][3]=0;
				  for(i=0;i<=(n-1)/2-1;i++)
					{  if(pos[i]<0) Bounds1[n-1][1]=Bounds1[n-1][1]+weight[i];
					   if(pos[i]>0) Bounds1[n-1][3]=Bounds1[n-1][3]+weight[i];
					}
				  Bounds1[n-1][0]=Bounds1[n-1][1]+p;
				  Bounds1[n-1][2]=Bounds1[n-1][3]+p;
				};
			}
		
		Ucdf = 1.0;
		Uccdf=1.0; Lcdf = 0.0; Lccdf=0.0;
		for(n=1;n<=NMoments;n++)
			{Ucdf =Math.min(Ucdf,  Bounds1[n-1][0]);
			 Lcdf =Math.max(Lcdf,  Bounds1[n-1][1]);
			 Uccdf=Math.min(Uccdf, Bounds1[n-1][2]);
	         Lccdf=Math.max(Lccdf, Bounds1[n-1][3]);
			}
		return new double[]{Ucdf, Lcdf, Uccdf, Lccdf};
	} 


	////////////////////////////////////////////////////

	// n! szamitasa kis ertekekre
	public static double Factorial(int n)
	{ 
	    double	res;
		int		i;

		if(n==0||n==1) return 1;

		res=1.0;
	    for(i=2;i<=n;i++) res=res*i;

		return res;

	} // End of Factorial


	// Binomialis egyutthato szamitasa
	public static double Binomial(int n, int k) {return Factorial(n)/(Factorial(n-k)*Factorial(k));}

	// Momentumok transzformacioja
	public static void MoveMoments(int NMoments, double mom[], double res_mom[], double A, double B)
	{
	    // A-val beszorozza
		// B-vel eltolja
		
		int		i, j;

		double[] tmp_mom = new double[21];

		// skalazas
		tmp_mom[0]=mom[0];
		for (i=1;i<NMoments;i++) tmp_mom[i] = mom[i] * Math.pow(A,i);		

		// eltolás
		for(i=0;i<NMoments;i++) 
			{res_mom[i]=0;
			 for(j=0;j<=i;j++)
			  res_mom[i] = res_mom[i] + Binomial(i,j) * tmp_mom[j] * Math.pow(B,i-j);
			}


	}  // End of MoveMoments


	// Momentumok ellenrozese (-INF,INF) eseten
	public static int CheckMoments1(int NMoments, double mom[])
	{

		int dim, row, col;
		RealMatrix M;

		if(NMoments%2==0) return NO;  

		dim=(NMoments-1)/2+1;

		while(dim>=1)
			{	M = MatrixUtils.createRealMatrix(dim,dim);
				for(row=0;row<dim;row++)
					for(col=0;col<dim;col++)
						M.setEntry(row,col,mom[row+col]);
					if(new LUDecomposition(M).getDeterminant()<=0) {return NO;}
				dim--;
			}
		
		return OK;
	}

	// Hankel determinans szamitasa
	public static double  Alpha(int size, double a[])
	{
	  int N,i,j;

	  N=size/2+1;

	  //TMatrix<double> A(N,N);
	  RealMatrix A = MatrixUtils.createRealMatrix(N,N);

	  for(i=0;i<N;i++)
		  for(j=0;j<N;j++)
			  A.setEntry(i, j, a[i+j]);

	   return new LUDecomposition(A).getDeterminant();

	} // End of Alpha


	// Maximalis tomeg kiszamitasa (-INF,INF) esetben a 0 pontban
	public static double  Mass1(int NMoments, double mom[])
	{
		int		i;
		double  p,sz,n;

		double[] szamlalo = new double[21];
		double[] nevezo = new double[21];

		if(NMoments%2==0) return 2.0;

		for(i=0;i<NMoments;  i++)   szamlalo[i]=mom[i];
		for(i=0;i<NMoments-2;i++)   nevezo[i]=mom[i+2];

		sz=	Alpha(NMoments,szamlalo);
		n=	Alpha(NMoments-2,nevezo);

	    if(n!=0) {p = sz/n;} else {p=2.0;};

		return p;

	} // End of Mass1


	// Vandermonde egyenletrendszer megoldasa
	public static void Vandermonde(int dim, double[] x, double[] w, double mom[])
	{
		int		i, j, k;
		double	b, s, t, xx;
		double[]  c;

		// ha 1 dimenziósak a vektorok
		if (dim == 1) { w[0] = mom[0]; return; }

		c = new double[dim];
		
		for(i=1;i<=dim;i++) c[i-1] = 0.0;
		c[dim-1] = -x[0];

		for (i=2; i<=dim; i++) 
			{ xx = -x[i-1];
			  for(j=(dim+1-i);j<=(dim-1);j++) c[j-1] += xx*c[j];		
			  c[dim-1] += xx;
			}

		for (i=1; i<=dim; i++) 
			{ xx = x[i-1];
			  t=b=1.0;
			  s=mom[dim-1];
			  for(k=dim;k>=2;k--)
				{ b=c[k-1]+xx*b;
				  s += mom[k-2]*b;
				  t=xx*t+b;
				}
			  if(t!=0) {w[i-1] = s/t;} else {w[i-1]=-1;}
			}


	}  // End of Vandermonde

	// Diskret eloszlas konstrualasa momentumok alapjan
	public static void  DiscreteConstruction(int NMoments, double mom[], double x[], double w[])
	{

		int						N,dim,i,j,col,c;
		double					[]coef;

		
		N=NMoments;

		if(N==1) {x[0]=0; w[0]=mom[0]; return;}

		if(N%2==1) N=N-1;			// Eldobunk egy momentumot
		dim = N/2;

		RealMatrix M;
		coef  = new double[dim+1];  // polinom egyutthatoi
		

		M = MatrixUtils.createRealMatrix(dim,dim);

		for(c=dim;c>=0;c--)
			{for(i=0;i<dim;i++)  // Sorok
				{ col=0;
				  for(j=0;j<=dim;j++) // Oszlopok
					if(j!=c) 
						{M.setEntry(i,col,mom[i+j]); col++;}
				}
			 coef[dim-c]=Math.pow(-1,c)*new LUDecomposition(M).getDeterminant();
			}
		Complex[] zero = new LaguerreSolver().solveAllComplex(coef, Math.random());

		for(i=0;i<dim;i++) x[i]=zero[i].getReal();

		Vandermonde(dim,x,w,mom);
	} // End of DiscreteConstruction
}
