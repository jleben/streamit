import streamit.*;
import streamit.io.*;

class source extends Filter {
    int N;
    float[] r;
    public source(float[] r) {super(r);}
    public void init(float[] r){
	output = new Channel(Float.TYPE,r.length);
	this.r=r;
	N=r.length;
    }
    public void work(){
	for(int i=0;i<N;i++)
	     output.pushFloat(r[i]);
    }

}

class sink extends Filter{
    int N;
    public sink(int N) {super(N);}
    public void init(int N){
	input = new Channel(Float.TYPE, N);
	this.N=N;
	//setPop(N);
    }
    public void work() {
	System.out.println("Starting");

	for (int i=0; i< N;i++)
	    {
		//System.out.print("This is ");
		//System.out.print(i);
		//System.out.print(" : ");
		System.out.println(input.popFloat());
}

	    
    }

}

class FBtest extends StreamIt {
    
 
    static public void main(String[] t)
    {
	FBtest test=new FBtest();
	test.run(t);
    }
    
   

    public void init() {
	int N_sim=1024*2;
	int N_samp=32;
	int N_ch=N_samp;
	int N_col=32;

	float[] r=new float[N_sim];
	float[][] H=new float[N_ch][N_col];
	float[][] F=new float[N_ch][N_col];
		
	for (int i=0;i<N_sim;i++)
	    r[i]=i+1;

	//float sum=0;	

	for (int i=0;i<N_col;i++) {
	    //sum+=1;
	    //sum=sum/7;

	    for (int j=0;j<N_ch;j++){
		//sum+=1;
		H[j][i]=i*N_col+j*N_ch+j+i+j+1;
		//sum++;
		F[j][i]=i*j+j*j+j+i;
	
	    }
	}
    
	

	
	add (new source(r));
	add (new FilterBank(N_samp,N_ch,N_col,H,F));
	add (new sink(r.length));
    }
    
    
}

	















