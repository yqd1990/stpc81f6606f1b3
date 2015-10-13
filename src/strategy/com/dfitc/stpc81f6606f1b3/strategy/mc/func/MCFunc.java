package com.dfitc.stpc81f6606f1b3.strategy.mc.func;

import com.dfitc.stp.market.Unit;
import com.dfitc.stp.strategy.BaseStrategy;
import com.dfitc.stp.util.BooleanSeries;
import com.dfitc.stp.util.DoubleSeries;
import com.dfitc.stp.util.StringSeries;
import com.dfitc.stp.util.StpLog;
import com.dfitc.stp.strategy.MC;

/**
 * 用户定义函数类
 */
public class MCFunc extends MC {
	/**
	 * @param b
	 * @param log
	 * @param className
	 * @exclude
	 */
	public MCFunc(BaseStrategy b, StpLog log, String className) {
		super(b, log, className);
	}

	/*************************************************
	 * 自定义函数
	 ************************************************/

	/**
	 * 计算数列的相关系数（只用一个数列）
	 * 
	 * @param dep
	 * @param len
	 * @return 相关系数
	 */
	public double coefficientreasy(DoubleSeries dep, double len){
		double coefficientreasy;
		DoubleSeries var0 = newDoubleSeries(0, varsName("var0"));
		DoubleSeries var1 = newDoubleSeries(0, varsName("var1"));
		DoubleSeries var2 = newDoubleSeries(0, varsName("var2"));
		DoubleSeries var3 = newDoubleSeries(0, varsName("var3"));
		DoubleSeries var4 = newDoubleSeries(0, varsName("var4"));
		DoubleSeries var5 = newDoubleSeries(0, varsName("var5"));
		DoubleSeries var6 = newDoubleSeries(0, varsName("var6"));
		DoubleSeries var7 = newDoubleSeries(0, varsName("var7"));
		DoubleSeries var8 = newDoubleSeries(0, varsName("var8"));
		DoubleSeries var9 = newDoubleSeries(0, varsName("var9"));

		coefficientreasy = (0 - 2);
		if(len > 0){
			var0.set(0.5 * (len - 1));
			var1.set(0);
			var1.set(var1.get(1) / len);
			var4.set(0);
			var2.set(0);
			var3.set(0);
			var9.set(var4.get(1) * var2.get(1));
			if(var9.get(1) > 0){
				var7.set(var3.get(1) / squareroot(var9.get(1)));
				condition1 = var7.get(1) >= (0 - 1) && var7.get(1) <= 1;
				if(condition1)
					coefficientreasy = var7.get(1);
			}
		}

		return coefficientreasy;
	}

	/**
	 * 晨星
	 * 
	 * @param tail
	 * @return ShootingStar
	 */
	public boolean shootingstar(DoubleSeries tail){
		boolean shootingstar;
		DoubleSeries var0 = newDoubleSeries(0, varsName("var0"));
		DoubleSeries var1 = newDoubleSeries(0, varsName("var1"));

		var0.set(minlist(close(), open()));
		var1.set(maxlist(close(), open()));
		shootingstar = false;
		if(currentbar() > 1){
			condition1 = var1.get(1) < medianprice() && open() != close();
			if(condition1){
				condition1 = high() - var1.get(1) > (var1.get(1) - var0.get(1)) * tail.get(1) && var0.get(1) - low() < var1.get(1) - var0.get(1);
				if(condition1)
					shootingstar = true;
			}
		}

		return shootingstar;
	}

	/**
	 * 真实波幅，可自订计算的数列
	 * 
	 * @param pricevalueh
	 * @param pricevaluel
	 * @param pricevaluec
	 * @return 真实波幅
	 */
	public double truerangecustom(double pricevalueh, double pricevaluel, DoubleSeries pricevaluec){
		double truerangecustom;
		DoubleSeries var0 = newDoubleSeries(0, varsName("var0"));
		DoubleSeries var1 = newDoubleSeries(0, varsName("var1"));

		var0.set(pricevalueh);
		var1.set(pricevaluel);
		if(pricevaluec.get(1) > pricevalueh)
			var0.set(pricevaluec.get(1));
		else if(pricevaluec.get(1) < pricevaluel)
			var1.set(pricevaluec.get(1));
		truerangecustom = var0.get(1) - var1.get(1);

		return truerangecustom;
	}

	/**
	 * 计算 K棒中价
	 * @return price
	 */
	public double medianprice(){
		double medianprice;

		medianprice = (high() + low()) * .5;

		return medianprice;
	}
	
}