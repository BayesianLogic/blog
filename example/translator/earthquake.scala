import com.cra.figaro._;
import language._;
import algorithm.sampling._;
import library.compound._;
import library.atomic.continuous._;
import library.atomic.discrete._;

object BLOG_FIGARO_TRANSLATED_RES {
Universe.createNew();

val B_Burglary = Flip(0.001);
val B_Earthquake = Flip(0.002);
val _IV0 = CPD(B_Burglary,B_Earthquake,(true,true)->Flip(0.95),(true,false)->Flip(0.94),(false,true)->Flip(0.29),(false,false)->Flip(0.001));
val B_Alarm = _IV0;
val _IV1 = CPD(B_Alarm,true->Flip(0.9),false->Flip(0.05));
val B_JohnCalls = _IV1;
val _IV2 = CPD(B_Alarm,true->Flip(0.7),false->Flip(0.01));
val B_MaryCalls = _IV2;
B_JohnCalls.observe(true);
B_MaryCalls.observe(true);
def main(args : Array[String]) {
val _IV3 = B_Burglary;
val F_0 = Importance(50000,_IV3);
F_0.start();
F_0.stop();
println("Distribution of Burglary:");
val F__DIS0 = F_0.distribution(_IV3);
for ( i <- 0 until F__DIS0.length) {
println("" + F__DIS0(i));
};
F_0.kill();

};
};
