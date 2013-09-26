import com.cra.figaro._;
import language._;
import algorithm.sampling._;
import library.compound._;
import library.atomic.continuous._;
import library.atomic.discrete._;

object BLOG_FIGARO_TRANSLATED_RES {
Universe.createNew();

class City(__name:Symbol) extends ElementCollection {
val _name=__name;
};
val B_A = new City('A);
val B_B = new City('B);
val B_N_City = Constant(2);
val B_AI_City = Array(B_A,B_B);

class PrepLevel(__name:Symbol) extends ElementCollection {
val _name=__name;
};
val B_Low = new PrepLevel('Low);
val B_High = new PrepLevel('High);
val B_N_PrepLevel = Constant(2);
val B_AI_PrepLevel = Array(B_Low,B_High);

class DamageLevel(__name:Symbol) extends ElementCollection {
val _name=__name;
};
val B_Severe = new DamageLevel('Severe);
val B_Mild = new DamageLevel('Mild);
val B_N_DamageLevel = Constant(2);
val B_AI_DamageLevel = Array(B_Severe,B_Mild);

val _IV0 = Apply(B_N_City,(n:Int)=>n==0);
val _IV1 = If(_IV0,Select(1.0->null.asInstanceOf[City]),Apply(IntSelector(B_N_City), (pos:Int)=>B_AI_City(pos)));
val B_First = _IV1;
val _IV2 = Apply(B_N_City,(n:Int)=>n==0);
val _IV3 = If(_IV2,Select(1.0->null.asInstanceOf[City]),Apply(IntSelector(B_N_City), (pos:Int)=>B_AI_City(pos)));
val B_Second = _IV3;
val B_PrepFirst = Select(0.5->B_High,0.5->B_Low);
val _IV4 = Apply(B_First,B_Second,(x:City,y:City)=>x==y);
val B_isSame = _IV4;
val _IV5 = CPD(B_PrepFirst,B_High->Select(0.2->B_Severe,0.8->B_Mild),B_Low->Select(0.8->B_Severe,0.2->B_Mild));
val B_DamageFirst = _IV5;
val _IV6 = CPD(B_DamageFirst,B_Severe->Select(0.9->B_High,0.1->B_Low),B_Mild->Select(0.1->B_High,0.9->B_Low));
val B_PrepSecond = _IV6;
val _IV7 = CPD(B_PrepSecond,B_High->Select(0.2->B_Severe,0.8->B_Mild),B_Low->Select(0.8->B_Severe,0.2->B_Mild));
val B_DamageSecond = _IV7;
val _IV8 = Apply(B_First,Select(1.0->B_A),(x:City,y:City)=>x==y);
val B_DamageA = If(_IV8,B_DamageFirst,B_DamageSecond);
val _IV9 = Apply(B_First,Select(1.0->B_B),(x:City,y:City)=>x==y);
val B_DamageB = If(_IV9,B_DamageFirst,B_DamageSecond);
val _IV10 = Apply(B_DamageFirst,Select(1.0->B_Severe),(a:DamageLevel,b:DamageLevel)=>a==b);
_IV10.observe(true);
B_isSame.observe(false);
def main(args : Array[String]) {
val _IV11 = B_First;
val F_0 = Importance(50000,_IV11);
F_0.start();
F_0.stop();
println("Distribution of First:");
val F__DIS0 = F_0.distribution(_IV11);
for ( i <- 0 until F__DIS0.length) {
println("( "+ F__DIS0(i)._1 +" , " + F__DIS0(i)._2._name +" )");
};
F_0.kill();
val _IV12 = B_DamageA;
val F_1 = Importance(50000,_IV12);
F_1.start();
F_1.stop();
println("Distribution of DamageA:");
val F__DIS1 = F_1.distribution(_IV12);
for ( i <- 0 until F__DIS1.length) {
println("( "+ F__DIS1(i)._1 +" , " + F__DIS1(i)._2._name +" )");
};
F_1.kill();
val _IV13 = B_DamageB;
val F_2 = Importance(50000,_IV13);
F_2.start();
F_2.stop();
println("Distribution of DamageB:");
val F__DIS2 = F_2.distribution(_IV13);
for ( i <- 0 until F__DIS2.length) {
println("( "+ F__DIS2(i)._1 +" , " + F__DIS2(i)._2._name +" )");
};
F_2.kill();
val _IV14 = B_DamageSecond;
val F_3 = Importance(50000,_IV14);
F_3.start();
F_3.stop();
println("Distribution of DamageSecond:");
val F__DIS3 = F_3.distribution(_IV14);
for ( i <- 0 until F__DIS3.length) {
println("( "+ F__DIS3(i)._1 +" , " + F__DIS3(i)._2._name +" )");
};
F_3.kill();

};
};
