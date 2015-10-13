package com.dfitc.stpc81f6606f1b3.strategy.tb.func;

import com.dfitc.stp.market.Unit;
import com.dfitc.stp.strategy.BaseStrategy;
import com.dfitc.stp.util.BooleanSeries;
import com.dfitc.stp.util.DoubleSeries;
import com.dfitc.stp.util.StringSeries;
import com.dfitc.stp.util.StpLog;
import com.dfitc.stp.strategy.TB;

/**
 * 用户定义函数类
 */
public class TBFunc extends TB {
	/**
	 * @param b
	 * @param log
	 * @param className
	 * @exclude
	 */
	public TBFunc(BaseStrategy b, StpLog log, String className){
		super(b, log, className);
	}

	/*************************************************
	 * 自定义函数
	 ************************************************/

	/**
	 * 求卡夫曼自适应移动平均。
	 * 
	 * @param price
	 * @param EffRatioLength
	 * @param FastAvgLength
	 * @param SlowAvgLength
	 * @return 数值型序列值的卡夫曼自适应移动平均值
	 */
	public double adaptivemovavg(DoubleSeries price, double EffRatioLength, double FastAvgLength, double SlowAvgLength) {
		double NetChg = 0;
		double TotChg = 0;
		double EffRatio = 0;
		double ScaledSFSqr = 0;
		DoubleSeries AMAValue = newDoubleSeries(0, varsName("AMAValue"));
		double SFDiff;

		if(currentbar() == 0){
			AMAValue.set(price.get());
		}else{
			NetChg = Math.abs(price.get() - price.get((int)EffRatioLength));

			DoubleSeries absPrice = new DoubleSeries();
			for(int i = 0; i < EffRatioLength; i++){
				absPrice.add(Math.abs(price.get(i) - price.get(i + 1)));
			}

			TotChg = summation(absPrice, EffRatioLength);
			EffRatio = iif(TotChg > 0, NetChg / TotChg, 0);
			SFDiff = 2 / (FastAvgLength + 1) - 2 / (SlowAvgLength + 1);
			ScaledSFSqr = sqr(2 / (SlowAvgLength + 1) + EffRatio * SFDiff);
			AMAValue.set(AMAValue.get(1) + ScaledSFSqr * (price.get() - AMAValue.get(1)));
		}
		return AMAValue.get();
	}

	/**
	 * 求平均。
	 * 
	 * @param num
	 * @param length
	 * @return 求数值型序列前length长度的平均值
	 */
	public double average(DoubleSeries num, double length) {
		int ilength = (int)(length + 0.5);
		if(ilength >= num.size())
			return Double.NaN;
		double sum = 0;
		while(ilength-- > 0){
			sum += num.get(ilength);
		}
		return sum / length;
	}

	/**
	 * 求n天以来的价格平均值
	 * 
	 * @param datatype
	 *            1 - Close, 2 - Open, 3 - High, 4 - Low, 5 - Vol, 6 - OpenInt
	 * @param length
	 * @return 指定价格类型n天以来的价格平均值
	 */
	public double averaged(double datatype, double length) {
		double sumvalue = 0;
		double value1 = 0.0;

		for(int i = (int)length - 1; i >= 0; i--){
			if((int)datatype == 1)
				value1 = closed(i);
			else if((int)datatype == 2)
				value1 = opend(i);
			else if((int)datatype == 3)
				value1 = highd(i);
			else if((int)datatype == 4)
				value1 = lowd(i);
			else if((int)datatype == 5)
				value1 = vold(i);
			else if((int)datatype == 6)
				value1 = openintd(i);
			if(value1 == invalidnumeric())
				return invalidnumeric();
			sumvalue += value1;
		}
		return sumvalue / length;
	}

	/**
	 * 快速计算平均值
	 */
	public double averagefc(DoubleSeries price, double length) {
		double avgvalue = 0.0;

		avgvalue = summationfc(price, length) / length;
		return avgvalue;
	}

	/**
	 * 求平均背离
	 */
	public double avgdeviation(DoubleSeries price, double length) {
		double sumvalue = 0;
		double mean = 0.0;
		double i = 0.0;

		mean = average(price, length);
		for(i = 0; i <= length - 1; i++){
			sumvalue = sumvalue + Math.abs(price.get((int)(i)) - mean);
		}
		return sumvalue / length;
	}

	/**
	 * 求平均价格
	 */
	public double avgprice() {
		double avgpricevalue = 0.0;

		if(open() > 0){
			avgpricevalue = (close() + open() + high() + low()) * 1 / 4;
		}else{
			avgpricevalue = (close() + high() + low()) * 1 / 3;
		}
		return avgpricevalue;
	}

	// /**
	// * 求平均真实范围
	// */
	// public double avgtruerange(double length) {
	// return average(truerange(), length);
	//
	// }

	public double[] boll(DoubleSeries price, double length, double offset){
		double midLine = average(close, length);
		double band = standarddev(close, length, 2);
		
		double[] results = new double[3];
		results[0] = midLine + offset * band; //上轨
		results[1] = midLine - offset * band; //下轨
		results[2] = midLine;				  //中间线
		
		return results;
	}

	/**
	 * 当天的第一个数据到当前的bar数
	 */
	public double barssincetoday() {
		DoubleSeries rebars = newDoubleSeries(0.0, varsName("rebars"));

		if(currentbar() == 0 || truedate(0) != truedate(1)){
			rebars.set(0);
		}else{
			rebars.set(rebars.get(1) + 1);
		}
		return rebars.get();

	}

	/**
	 * 求n天前的收盘价
	 */
	public double closed(double daysago) {
		DoubleSeries barcnt = newDoubleSeries(0.0, varsName("barcnt"));
		DoubleSeries dayclose = newDoubleSeries(0.0, varsName("dayclose"));
		double i = 0.0;
		double j = 0.0;
		double nindex = 0;
		double cbindex = 0.0;

		cbindex = currentbar();
		if(cbindex == 0 || truedate(0) != truedate(1)){
			barcnt.set(1);
		}else{
			barcnt.set(barcnt.get(1) + 1);
		}
		dayclose.set(close());
		if(daysago == 0){
			return dayclose.get();
		}
		for(i = 1; i <= daysago; i++){
			if(i != 1)
				j += barcnt.get((int)(j));
			if(j > cbindex)
				return invalidnumeric();
			nindex += barcnt.get((int)(j));
		}
		return dayclose.get((int)(nindex));
	}

	/**
	 * 求皮尔森相关系数。
	 */
	public double coefficientr(DoubleSeries price1, DoubleSeries price2, double length) {
		double AvgX = 0;
		double AvgY = 0;
		double SumDySqr = 0;
		double SumDxDy = 0;
		double SumDxSqr = 0;
		double Dy = 0;
		double Dx = 0;
		double CoefR = -2;
		double tmp;

		if(length > 0){
			AvgX = 0;
			AvgY = 0;
			for(int i = 0; i < length; i++){
				AvgX = AvgX + price1.get(i);
				AvgY = AvgY + price2.get(i);
			}
			AvgX = AvgX / length;
			AvgY = AvgY / length;

			for(int i = 0; i < length; i++){
				Dx = price1.get(i) - AvgX;
				Dy = price2.get(i) - AvgY;
				SumDxSqr = SumDxSqr + sqr(Dx);
				SumDySqr = SumDySqr + sqr(Dy);
				SumDxDy = SumDxDy + Dx * Dy;
			}

			tmp = SumDxSqr * SumDySqr;
			if(tmp > 0){
				tmp = SumDxDy / Math.sqrt(tmp);
				if(tmp >= -1 && tmp <= 1){
					CoefR = tmp;
				}
			}
		}
		return CoefR;
	}

	/**
	 * 求相关系数
	 */
	public double correlation(DoubleSeries price1, DoubleSeries price2, double length) {
		double matches = 0;
		BooleanSeries con = newBooleanSeries(false, varsName("con"));
		double i = 0.0;

		con.set((price1.get() >= price1.get(1) && price2.get() >= price2.get(1)) || (price1.get() < price1.get(1) && price2.get() < price2.get(1)));
		if(currentbar() < length){
			return 0;
		}else{
			for(i = 0; i <= length - 1; i++){
				if(con.get((int)(i))){
					matches++;
				}
			}
			return 2 * matches / length - 1;
		}
	}

	/**
	 * 获取最近n周期条件满足的计数
	 */
	public double countif(BooleanSeries testcondition, double length) {
		double sum = 0;
		double i = 0.0;

		for(i = 0; i <= length - 1; i++){
			if(testcondition.get((int)(i)))
				sum++;
		}
		return sum;
	}

	/**
	 * 求协方差
	 */
	public double covar(DoubleSeries price1, DoubleSeries price2, double length) {
		double mean1 = 0.0;
		double mean2 = 0.0;
		double sumvalue = 0;
		double i = 0.0;

		if(length > 0){
			mean1 = average(price1, length);
			mean2 = average(price2, length);
			for(i = 0; i <= length - 1; i++){
				sumvalue = sumvalue + (price1.get((int)(i)) - mean1) * (price2.get((int)(i)) - mean2);
			}
			return sumvalue / length;
		}
		return -1;

	}

	/**
	 * 求是否上穿
	 */
	public boolean crossover(DoubleSeries price1, DoubleSeries price2) {
		boolean con1 = false;
		boolean precon = false;
		double counter = 0;

		if(price1.get() > price2.get()){
			counter = 1;
			con1 = price1.get(1) == price2.get(1);
			while(con1 && counter < currentbar()){
				counter = counter + 1;
				con1 = price1.get((int)(counter)) == price2.get((int)(counter));
			}
			precon = price1.get((int)(counter)) < price2.get((int)(counter));
			return precon;
		}
		return false;
	}

	/**
	 * 求是否下破
	 */
	public boolean crossunder(DoubleSeries price1, DoubleSeries price2) {
		boolean con1 = false;
		boolean precon = false;
		double counter = 0;

		if(price1.get() < price2.get()){
			counter = 1;
			con1 = price1.get(1) == price2.get(1);
			while(con1 && counter < currentbar()){
				counter = counter + 1;
				con1 = price1.get((int)(counter)) == price2.get((int)(counter));
			}
			precon = price1.get((int)(counter)) > price2.get((int)(counter));
			return precon;
		}else{
			return false;
		}
	}

	/**
	 * 求累计值
	 */
	public double cum(Double price) {
		DoubleSeries cumvalue = newDoubleSeries(0.0, varsName("cumvalue"));

		if(currentbar() == 0){
			cumvalue.set(price);
		}else{
			cumvalue.set(cumvalue.get(1) + price);
		}
		return cumvalue.get();
	}

	/**
	 * 求双指数移动平均。
	 */
	public double dema(DoubleSeries price, double length) {
		DoubleSeries ema1 = newDoubleSeries(0.0, varsName("ema1"));
		double ema2 = 0.0;

		ema1.set(xaverage(price.get(), length));
		ema2 = xaverage(ema1.get(), length);
		return 2 * ema1.get() - ema2;
	}

	/**
	 * 求趋势平滑。
	 */
	public double detrend(DoubleSeries price, double length) {
		double sumvalue = 0;
		double mean = 0.0;
		double i = 0.0;

		mean = average(price, length);
		for(i = 0; i <= length - 1; i++){
			sumvalue = sumvalue + sqr(price.get((int)(i)) - mean);
		}
		return sumvalue;
	}

	/**
	 * 求偏差均方和。
	 */
	public double devsqrd(DoubleSeries price, double length) {
		double sumvalue = 0;
		double mean = 0.0;
		double i = 0.0;

		mean = average(price, length);
		for(i = 0; i <= length - 1; i++){
			sumvalue = sumvalue + sqr(price.get((int)(i)) - mean);
		}
		return sumvalue;

	}

	/**
	 * 求极值
	 */
	public double extremes(DoubleSeries price, double length, boolean bmax, double extremebar) {
		DoubleSeries myval = newDoubleSeries(0.0, varsName("myval"));
		DoubleSeries mybar = newDoubleSeries(0.0, varsName("mybar"));
		double i = 0.0;

		myval.set(price.get());
		mybar.set(0);
		if(currentbar() <= length - 1 || mybar.get(1) == length - 1){
			for(i = 1; i <= length - 1; i++){
				if(bmax){
					if(price.get((int)(i)) > myval.get()){
						myval.set(price.get((int)(i)));
						mybar.set(i);
					}
				}else{
					if(price.get((int)(i)) < myval.get()){
						myval.set(price.get((int)(i)));
						mybar.set(i);
					}
				}
			}
		}else{
			if(bmax){
				if(price.get() >= myval.get(1)){
					myval.set(price.get());
					mybar.set(0);
				}else{
					myval.set(myval.get(1));
					mybar.set(mybar.get(1) + 1);
				}
			}else{
				if(price.get() <= myval.get(1)){
					myval.set(price.get());
					mybar.set(0);
				}else{
					myval.set(myval.get(1));
					mybar.set(mybar.get(1) + 1);
				}
			}
		}
		extremebar = mybar.get();
		return myval.get();
	}

	/**
	 * 求fisher变换
	 */
	public double fisher(DoubleSeries price) {
		double fishervalue = 0.0;

		if(price.get() > -1 && price.get() < 1){
			fishervalue = ln((1 + price.get()) / (1 - price.get())) * 0.5;
		}else{
			fishervalue = -999;
		}
		return fishervalue;
	}

	/**
	 * 求反fisher变换
	 */
	public double fisherinv(DoubleSeries price) {
		double fisherinvvalue = 0.0;

		fisherinvvalue = (Math.exp(2 * price.get()) - 1) / (Math.exp(2 * price.get()) + 1);
		return fisherinvvalue;
	}

	/**
	 * 求调和平均数
	 */
	public double harmonicmean(DoubleSeries price, double length) {
		double harmeanvalue = 0.0;

		if(lowest(price, length) > 0){
			DoubleSeries rePrice = new DoubleSeries();
			for(int i = 0; i < length; i++)
				rePrice.add(1 / price.get(i));
			harmeanvalue = length / summation(rePrice, length);
		}else{
			harmeanvalue = -1;
		}
		return harmeanvalue;
	}

	/**
	 * 求n天前的最高价
	 */
	public double highd(double daysago) {
		DoubleSeries barcnt = newDoubleSeries(0.0, varsName("barcnt"));
		DoubleSeries dayhigh = newDoubleSeries(0.0, varsName("dayhigh"));
		double i = 0.0;
		double j = 0.0;
		double nindex = 0;
		double cbindex = 0.0;

		cbindex = currentbar();
		if(cbindex == 0 || truedate(0) != truedate(1)){
			barcnt.set(1);
			dayhigh.set(high());
		}else{
			barcnt.set(barcnt.get(1) + 1);
			if(high() > dayhigh.get())
				dayhigh.set(high());
		}
		if(daysago == 0){
			return dayhigh.get();
		}else{
			for(i = 1; i <= daysago; i++){
				if(i == 1){
					j = 0;
				}else{
					j = j + barcnt.get((int)(j));
				}
				if(j > cbindex)
					return invalidnumeric();
				nindex = nindex + barcnt.get((int)(j));
			}
			return dayhigh.get((int)(nindex));
		}
	}

	/**
	 * 求最高
	 */
	public double highest(DoubleSeries price, double length) {
		if(length > 1)
			return price.head((int)length).max();
		return price.get();
	}

	/**
	 * 求最高值出现的bar
	 */
	public double highestbar(DoubleSeries price, double length) {
		double highestvalue = 0.0;
		double i = 0.0;
		double rebar = 0;

		highestvalue = price.get();
		for(i = 1; i <= length - 1; i++){
			if(price.get((int)(i)) > highestvalue){
				highestvalue = price.get((int)(i));
				rebar = i;
			}
		}
		return rebar;
	}

	/**
	 * 求最高值出现的bar(快速计算版本)
	 */
	public double highestbarfc(DoubleSeries price, double length) {
		double highestvalue = 0.0;
		double extremesbar = 0.0;

		highestvalue = extremes(price, length, true, extremesbar);
		return extremesbar;
	}

	/**
	 * 求最高(快速计算版本)
	 */
	public double highestfc(DoubleSeries price, double length) {
		double highestvalue = 0.0;
		double extremesbar = 0.0;

		highestvalue = extremes(price, length, true, extremesbar);
		return highestvalue;
	}

	/**
	 * 求峰度系数。
	 */
	public double kurtosis(DoubleSeries Price, double length) {
		double KurtValue = 0;
		double P1 = 0;
		double P2 = 0;
		double P3 = 0;
		double Mean;
		double SDev;

		if(length > 3){
			Mean = average(Price, length);
			SDev = standarddev(Price, length, 2);
			if(SDev > 0){
				for(int i = 0; i < length; i++){
					P2 = P2 + power((Price.get(i) - Mean) / SDev, 4);
				}
				P1 = length * (length + 1) / ((length - 1) * (length - 2) * (length - 3));
				P3 = 3 * sqr(length - 1) / ((length - 2) * (length - 3));
				KurtValue = P1 * P2 - P3;
			}
		}
		return KurtValue;
	}

	/**
	 * 求线性回归
	 * 
	 * @param price
	 * @param length
	 * @param tgtbar
	 * @param lrslope
	 * @param lrangle
	 * @param lrintercept
	 * @param lrvalue
	 * @return 执行成功返回true，否则返回false
	 */
	public boolean linearreg(DoubleSeries price, double length, double tgtbar, double lrslope, double lrangle, double lrintercept, double lrvalue) {
		double sumxy = 0;
		double sumy = 0.0;
		double sumx = 0.0;
		double sumxsqr = 0.0;
		double divisor = 0.0;
		double i = 0.0;

		if(length > 1){
			sumx = length * (length - 1) * 1 / 2;
			sumxsqr = length * (length - 1) * (2 * length - 1) * 1 / 6;
			divisor = sqr(sumx) - length * sumxsqr;
			sumy = summation(price, length);
			for(i = 0; i <= length - 1; i++){
				sumxy = sumxy + i * price.get((int)(i));
			}
			lrslope = (length * sumxy - sumx * sumy) / divisor;
			lrangle = Math.atan(lrslope);
			lrintercept = (sumy - lrslope * sumx) / length;
			lrvalue = lrintercept + (length - 1 - tgtbar) * lrslope;
			return true;
		}else{
			return false;
		}
	}

	/**
	 * 求线性回归角度
	 * 
	 * @param price
	 * @param length
	 * @return 线性回归角度
	 */
	public double linearregangle(DoubleSeries price, double length) {
		double lrslope = 0.0;
		double lrangle = 0.0;
		double lrintercept = 0.0;
		double lrvalue = 0.0;

		linearreg(price, length, 0, lrslope, lrangle, lrintercept, lrvalue);
		return lrangle;
	}

	/**
	 * 求线性回归斜率
	 * 
	 * @param price
	 * @param length
	 * @return 线性回归斜率
	 */
	public double linearregslope(DoubleSeries price, double length) {
		double lrslope = 0.0;
		double lrangle = 0.0;
		double lrintercept = 0.0;
		double lrvalue = 0.0;

		linearreg(price, length, 0, lrslope, lrangle, lrintercept, lrvalue);
		return lrslope;
	}

	/**
	 * 求线性回归值
	 * 
	 * @param price
	 * @param length
	 * @param tgtbar
	 * @return 线性回归值
	 */
	public double linearregvalue(DoubleSeries price, double length, double tgtbar) {
		double lrslope = 0.0;
		double lrangle = 0.0;
		double lrintercept = 0.0;
		double lrvalue = 0.0;

		linearreg(price, length, tgtbar, lrslope, lrangle, lrintercept, lrvalue);
		return lrvalue;
	}

	/**
	 * 求n天前的最低价
	 * 
	 * @param daysago
	 * @return 最低价
	 */
	public double lowd(double daysago) {
		DoubleSeries barcnt = newDoubleSeries(0.0, varsName("barcnt"));
		DoubleSeries daylow = newDoubleSeries(0.0, varsName("daylow"));
		double i = 0.0;
		double j = 0.0;
		double nindex = 0;
		double cbindex = 0.0;

		cbindex = currentbar();
		if(cbindex == 0 || truedate(0) != truedate(1)){
			barcnt.set(1);
			daylow.set(low());
		}else{
			barcnt.set(barcnt.get(1) + 1);
			if(low() < daylow.get())
				daylow.set(low());
		}
		if(daysago == 0){
			return daylow.get();
		}else{
			for(i = 1; i <= daysago; i++){
				if(i == 1){
					j = 0;
				}else{
					j = j + barcnt.get((int)(j));
				}
				if(j > cbindex)
					return invalidnumeric();
				nindex = nindex + barcnt.get((int)(j));
			}
			return daylow.get((int)(nindex));
		}
	}

	/**
	 * 求最低
	 * 
	 * @param price
	 * @param length
	 * @return 最低值
	 */
	public double lowest(DoubleSeries price, double length) {
		if(length > 1)
			return price.head((int)length).min();
		return price.get();
	}

	/**
	 * 求最低值出现的bar
	 * 
	 * @param price
	 * @param length
	 * @return bar索引值
	 */
	public double lowestbar(DoubleSeries price, double length) {
		double lowestvalue = 0.0;
		double i = 0.0;
		double rebar = 0;

		lowestvalue = price.get();
		for(i = 1; i <= length - 1; i++){
			if(price.get((int)(i)) < lowestvalue){
				lowestvalue = price.get((int)(i));
				rebar = i;
			}
		}
		return rebar;
	}

	/**
	 * 求最低值出现的bar(快速计算版本)
	 */
	public double lowestbarfc(DoubleSeries price, double length) {
		double lowestvalue = 0.0;
		double extremesbar = 0.0;

		lowestvalue = extremes(price, length, false, extremesbar);
		return extremesbar;
	}

	/**
	 * 求最低(快速计算版本)
	 */
	public double lowestfc(DoubleSeries price, double length) {
		double lowestvalue = 0.0;
		double extremesbar = 0.0;

		lowestvalue = extremes(price, length, false, extremesbar);
		return lowestvalue;
	}

	/**
	 * 求中位数
	 */
	public double median(DoubleSeries price, double length) {
		double mediavalue1 = 0.0;
		double mediavalue2 = 0.0;
		double halflength = 0.0;
		double tmpbar = 0.0;

		if(length % 2 == 0){
			halflength = length / 2;
			mediavalue1 = nthextremes(price, length, halflength, true, tmpbar);
			mediavalue2 = nthextremes(price, length, halflength + 1, true, tmpbar);
			return (mediavalue1 + mediavalue2) / 2;
		}else{
			mediavalue1 = nthextremes(price, length, (length + 1) / 2, true, tmpbar);
			return mediavalue1;
		}
	}

	/**
	 * 求中点
	 */
	public double midpoint(DoubleSeries price, double length) {
		return (highest(price, length) + lowest(price, length)) / 2;
	}

	/**
	 * 求众数
	 */
	public double mode(DoubleSeries price, double length) {
		double modevalue = 1;
		double modecounter = 1;
		double tmpvalue = 0.0;
		double tmpcounter = 0.0;
		double i = 0.0;
		double j = 0.0;

		if(length > 1){
			for(i = 0; i <= length - 1; i++){
				tmpcounter = 0;
				tmpvalue = price.get((int)(i));
				if(tmpvalue != modevalue){
					for(j = 0; j <= length - 1; j++){
						if(tmpvalue == price.get((int)(j))){
							tmpcounter++;
						}
					}
					if(tmpcounter > modecounter){
						modecounter = tmpcounter;
						modevalue = tmpvalue;
					}
				}
			}
		}
		return modevalue;
	}

	/**
	 * 求动量
	 */
	public double momentum(DoubleSeries price, double length) {
		return price.get() - price.get((int)length);
	}

	/**
	 * 第n个满足条件的bar距当前的bar数目
	 */
	public double nthcon(boolean con, double n) {
		DoubleSeries preconindex = newDoubleSeries(0.0, varsName("preconindex"));
		DoubleSeries barnums = newDoubleSeries(0.0, varsName("barnums"));
		double i = 0;
		double rebars = 0;

		if(con){
			barnums.set(0);
			preconindex.set(barnums.get(1) + 1);
		}else{
			barnums.set(barnums.get(1) + 1);
			preconindex.set(barnums.get());
		}
		rebars = barnums.get();
		for(i = 2; i <= n; i++){
			rebars = rebars + preconindex.get((int)(rebars));
		}
		return rebars;
	}

	/**
	 * 求n极值
	 */
	public double nthextremes(DoubleSeries price, double length, double n, boolean bmax, double nthextremebar) {
		double nmaxbar = 0.0;
		double nthmaxvalue = 0.0;
		double nminbar = 0.0;
		double nthminvalue = 0.0;
		double tmpvalue = 0.0;
		double nthreturnvalue = 0.0;
		double nbettercnt = 0.0;
		double nequalcnt = 0.0;
		double nequalindex = 0.0;
		double i = 0.0;
		double j = 0.0;
		double k = 0.0;

		if(length > 0 && n > 0 && n <= length){
			nthmaxvalue = extremes(price, length, true, nmaxbar);
			nthminvalue = extremes(price, length, false, nminbar);
			if(bmax){
				for(i = 2; i <= n; i++){
					tmpvalue = nthminvalue - 1;
					nbettercnt = 0;
					nequalcnt = 0;
					for(j = 0; j <= length - 1; j++){
						if(price.get((int)(j)) > nthmaxvalue){
							nbettercnt = nbettercnt + 1;
						}else if(price.get((int)(j)) < nthmaxvalue){
							if(price.get((int)(j)) > tmpvalue){
								tmpvalue = price.get((int)(j));
								nmaxbar = j;
							}
						}else{
							nequalcnt = nequalcnt + 1;
						}
					}
					if(nbettercnt + nequalcnt >= i){
						nequalindex = 0;
						for(k = 0; k <= length - 1; k++){
							if(price.get((int)(k)) == nthmaxvalue){
								nequalindex = nequalindex + 1;
								if(nequalindex == (i - nbettercnt)){
									nmaxbar = k;
								}
							}
						}
					}else{
						nthmaxvalue = tmpvalue;
					}
				}
				nthextremebar = nmaxbar;
				nthreturnvalue = nthmaxvalue;
			}else{
				for(i = 2; i <= n; i++){
					tmpvalue = nthmaxvalue + 1;
					nbettercnt = 0;
					nequalcnt = 0;
					for(j = 0; j <= length - 1; j++){
						if(price.get((int)(j)) < nthminvalue){
							nbettercnt = nbettercnt + 1;
						}else if(price.get((int)(j)) > nthminvalue){
							if(price.get((int)(j)) < tmpvalue){
								tmpvalue = price.get((int)(j));
								nminbar = j;
							}
						}else{
							nequalcnt = nequalcnt + 1;
						}
					}
					if(nbettercnt + nequalcnt >= i){
						nequalindex = 0;
						for(k = 0; k <= length - 1; k++){
							if(price.get((int)(k)) == nthminvalue){
								nequalindex = nequalindex + 1;
								if(nequalindex == (i - nbettercnt)){
									nminbar = k;
								}
							}
						}
					}else{
						nthminvalue = tmpvalue;
					}
				}
				nthextremebar = nminbar;
				nthreturnvalue = nthminvalue;
			}
		}else{
			nthreturnvalue = -1;
			nthextremebar = -1;
		}
		return nthreturnvalue;
	}

	/**
	 * 求第n高
	 */
	public double nthhigher(DoubleSeries price, double length, double n) {
		double nthhighervalue = 0.0;
		double nthextremesbar = 0.0;

		nthhighervalue = nthextremes(price, length, n, true, nthextremesbar);
		return nthhighervalue;
	}

	/**
	 * 求第n高出现的bar
	 */
	public double nthhigherbar(DoubleSeries price, double length, double n) {
		double nthhighervalue = 0.0;
		double nthextremesbar = 0.0;

		nthhighervalue = nthextremes(price, length, n, true, nthextremesbar);
		return nthextremesbar;
	}

	/**
	 * 求第n低
	 */
	public double nthlower(DoubleSeries price, double length, double n) {
		double nthlowervalue = 0.0;
		double nthextremesbar = 0.0;

		nthlowervalue = nthextremes(price, length, n, false, nthextremesbar);
		return nthlowervalue;
	}

	/**
	 * 求第n低出现的bar
	 */
	public double nthlowerbar(DoubleSeries price, double length, double n) {
		double nthlowervalue = 0.0;
		double nthextremesbar = 0.0;

		nthlowervalue = nthextremes(price, length, n, false, nthextremesbar);
		return nthextremesbar;
	}

	/**
	 * 求n天前的开盘价
	 */
	public double opend(double daysago) {
		DoubleSeries barcnt = newDoubleSeries(0.0, varsName("barcnt"));
		DoubleSeries dayopen = newDoubleSeries(0.0, varsName("dayopen"));
		double i = 0.0;
		double j = 0.0;
		double nindex = 0;
		double cbindex = 0.0;

		cbindex = currentbar();
		if(cbindex == 0 || truedate(0) != truedate(1)){
			barcnt.set(1);
			dayopen.set(open());
		}else{
			barcnt.set(barcnt.get(1) + 1);
		}
		if(daysago == 0){
			return dayopen.get();
		}
		for(i = 1; i <= daysago; i++){
			if(i != 1)
				j += barcnt.get((int)(j));
			if(j > cbindex)
				return invalidnumeric();
			nindex += barcnt.get((int)(j));
		}
		return dayopen.get((int)(nindex));
	}

	/**
	 * 求n天前的持仓量
	 */
	public double openintd(double daysago) {
		DoubleSeries barcnt = newDoubleSeries(0.0, varsName("barcnt"));
		DoubleSeries dayopenint = newDoubleSeries(0.0, varsName("dayopenint"));
		double i = 0.0;
		double j = 0.0;
		double nindex = 0;
		double cbindex = 0.0;

		cbindex = currentbar();
		if(cbindex == 0 || truedate(0) != truedate(1)){
			barcnt.set(1);
		}else{
			barcnt.set(barcnt.get(1) + 1);
		}
		dayopenint.set(openint());
		if(daysago == 0){
			return dayopenint.get();
		}
		for(i = 1; i <= daysago; i++){
			if(i == 1){
				j = 0;
			}else{
				j = j + barcnt.get((int)(j));
			}
			if(j > cbindex)
				return invalidnumeric();
			nindex = nindex + barcnt.get((int)(j));
		}
		return dayopenint.get((int)(nindex));

	}

	/**
	 * 求抛物线转向
	 */
	public boolean parabolicsar(double afstep, double aflimit, double oparclose, double oparopen, double oposition, double otransition) {
		DoubleSeries af = newDoubleSeries(0, varsName("af"));
		DoubleSeries paropen = newDoubleSeries(0, varsName("paropen"));
		DoubleSeries position = newDoubleSeries(0, varsName("position"));
		DoubleSeries hhvalue = newDoubleSeries(0, varsName("hhvalue"));
		DoubleSeries llvalue = newDoubleSeries(0, varsName("llvalue"));

		if(currentbar() == 0){
			position.set(1);
			otransition = 1;
			af.set(afstep);
			hhvalue.set(high());
			llvalue.set(low());
			oparclose = llvalue.get();
			paropen.set(oparclose + af.get() * (hhvalue.get() - oparclose));
			if(paropen.get() > low()){
				paropen.set(low());
			}
		}else{
			otransition = 0;
			if(high() > hhvalue.get(1)){
				hhvalue.set(high());
			}else{
				hhvalue.set(hhvalue.get(1));
			}
			if(low() < llvalue.get(1)){
				llvalue.set(low());
			}else{
				llvalue.set(llvalue.get(1));
			}
			if(position.get(1) == 1){
				if(low() <= paropen.get(1)){
					position.set(-1);
					otransition = -1;
					oparclose = hhvalue.get();
					hhvalue.set(high());
					llvalue.set(low());
					af.set(afstep);
					paropen.set(oparclose + af.get() * (llvalue.get() - oparclose));
					if(paropen.get() < high()){
						paropen.set(high());
					}
					if(paropen.get() < high(1)){
						paropen.set(high(1));
					}
				}else{
					position.set(position.get(1));
					oparclose = paropen.get(1);
					if(hhvalue.get() > hhvalue.get(1) && af.get(1) < aflimit){
						if(af.get(1) + afstep > aflimit){
							af.set(aflimit);
						}else{
							af.set(af.get(1) + afstep);
						}
					}else{
						af.set(af.get(1));
					}
					paropen.set(oparclose + af.get() * (hhvalue.get() - oparclose));
					if(paropen.get() > low()){
						paropen.set(low());
					}
					if(paropen.get() > low(1)){
						paropen.set(low(1));
					}
				}
			}else{
				if(high() >= paropen.get(1)){
					position.set(1);
					otransition = 1;
					oparclose = llvalue.get();
					hhvalue.set(high());
					llvalue.set(low());
					af.set(afstep);
					paropen.set(oparclose + af.get() * (hhvalue.get() - oparclose));
					if(paropen.get() > low()){
						paropen.set(low());
					}
					if(paropen.get() > low(1)){
						paropen.set(low(1));
					}
				}else{
					position.set(position.get(1));
					oparclose = paropen.get(1);
					if(llvalue.get() < llvalue.get(1) && af.get(1) < aflimit){
						if(af.get(1) + afstep > aflimit){
							af.set(aflimit);
						}else{
							af.set(af.get(1) + afstep);
						}
					}else{
						af.set(af.get(1));
					}
					paropen.set(oparclose + af.get() * (llvalue.get() - oparclose));
					if(paropen.get() < high()){
						paropen.set(high());
					}
					if(paropen.get() < high(1)){
						paropen.set(high(1));
					}
				}
			}
		}
		oparopen = paropen.get();
		oposition = position.get();
		return true;
	}

	/**
	 * 求涨跌幅
	 */
	public double percentchange(DoubleSeries price, double length) {
		double pcvalue = 0;

		if(price.get((int)(length)) != 0){
			pcvalue = (price.get() - price.get((int)(length))) / price.get((int)(length));
		}else{
			pcvalue = 0;
		}
		return pcvalue;
	}

	// /**
	// * 求威廉指标
	// */
	// public double percentr(double length) {
	// double hh = 0.0;
	// double divisor = 0.0;
	// double prvalue = 0.0;
	//
	// hh = highest(high(), length);
	// divisor = hh - lowest(low(), length);
	// if(divisor != 0)
	// prvalue = 100 - ((hh - close()) / divisor) * 100;
	// else
	// prvalue = divisor;
	// return prvalue;
	// }

	/**
	 * 求排列
	 */
	public double permutation(double num, double numchosen) {
		double permvalue = 0.0;

		if(numchosen >= 1 && num >= numchosen){
			permvalue = fact(num) / fact(num - intpart(numchosen));
		}else{
			permvalue = -1;
		}
		return permvalue;
	}

	/**
	 * 求转折
	 */
	public boolean pivot(DoubleSeries price, double length, double leftstrength, double rightstrength, double instance, double hilo, double pivotprice, double pivotbar) {
		double candidateprice = 0;
		double lengthcntr = 0;
		double strengthcntr = 0;
		double instancecntr = 0;
		boolean pivottest = false;
		boolean instancetest = false;

		instancecntr = 0;
		instancetest = false;
		lengthcntr = rightstrength;
		while(lengthcntr < length && (!instancetest)){
			candidateprice = price.get((int)(lengthcntr));
			pivottest = true;
			strengthcntr = lengthcntr + 1;
			while(pivottest && strengthcntr - lengthcntr <= leftstrength){
				if((hilo == 1 && candidateprice < price.get((int)(strengthcntr))) || (hilo == -1 && candidateprice > price.get((int)(strengthcntr))))
					pivottest = false;
				else
					strengthcntr = strengthcntr + 1;
			}
			strengthcntr = lengthcntr - 1;
			while(pivottest && (lengthcntr - strengthcntr) <= rightstrength){
				if((hilo == 1 && candidateprice <= price.get((int)(strengthcntr))) || (hilo == -1 && candidateprice >= price.get((int)(strengthcntr))))
					pivottest = false;
				else
					strengthcntr = strengthcntr - 1;
			}
			if(pivottest)
				instancecntr = instancecntr + 1;
			if(instancecntr == instance)
				instancetest = true;
			else
				lengthcntr = lengthcntr + 1;
		}
		if(instancetest){
			pivotprice = candidateprice;
			pivotbar = lengthcntr;
			return true;
		}else{
			pivotprice = -1;
			pivotbar = -1;
			return false;
		}
	}

	/**
	 * 求振荡
	 */
	public double priceoscillator(DoubleSeries price, double fastlength, double slowlength) {
		double povalue = 0.0;

		povalue = average(price, fastlength) - average(price, slowlength);
		return povalue;
	}

	/**
	 * 求变动率
	 */
	public double rateofchange(DoubleSeries price, double length) {
		double rocvalue = 0.0;

		if(price.get((int)(length)) != 0){
			rocvalue = (price.get() / price.get((int)(length)) - 1) * 100;
		}else{
			rocvalue = 0;
		}
		return rocvalue;
	}

	/**
	 * 求平滑平均
	 */
	public double saverage(DoubleSeries price, double length) {
		DoubleSeries savgvalue = newDoubleSeries(0.0, varsName("savgvalue"));

		if(currentbar() < length){
			savgvalue.set(summation(price, length) / length);
		}else{
			savgvalue.set((summation(price, length + 1) - savgvalue.get(1)) / length);
		}
		return savgvalue.get();
	}

	/**
	 * 求偏度系数
	 */
	public double skewness(DoubleSeries price, double length) {
		double skewvalue = 0;
		double sum = 0;
		double y = 0.0;
		double mean = 0.0;
		double sdev = 0.0;
		double i = 0.0;

		if(length > 2){
			mean = average(price, length);
			sdev = standarddev(price, length, 2);
			if(sdev > 0){
				for(i = 0; i <= length - 1; i++){
					sum = sum + power((price.get((int)(i)) - mean) / sdev, 3);
				}
				y = length / ((length - 1) * (length - 2));
				skewvalue = y * sum;
			}
		}
		return skewvalue;
	}

	/**
	 * 求移动平均
	 */
	public double sma(DoubleSeries price, double length, double weight) {
		DoubleSeries smavalue = newDoubleSeries(0.0, varsName("smavalue"));

		if(currentbar() == 0){
			smavalue.set(price.get());
		}else{
			smavalue.set((smavalue.get(1) * (length - weight) + price.get() * weight) / length);
		}
		return smavalue.get();
	}

	/**
	 * 求标准方差。
	 */
	public double standarddev(DoubleSeries Price, double length, double dataType) {
		double VarPSValue;

		VarPSValue = varianceps(Price, length, dataType);
		if(VarPSValue > 0){
			return Math.sqrt(VarPSValue);
		}else{
			return 0;
		}
	}

	/**
	 * 求和。
	 */
	public double summation(DoubleSeries num, double length) {
		if(num.size() > 0){
			double sum = 0;
			for(int i = 0; i < num.size(); i++)
				sum += num.get(i);
			return sum;
		}
		return 0;
	}

	/**
	 * 快速求和
	 */
	public double summationfc(DoubleSeries price, double length) {
		DoubleSeries sumvalue = newDoubleSeries(0, varsName("sumvalue"));
		double i = 0.0;

		if(currentbar() < length){
			sumvalue.set(0);
			for(i = 0; i <= length - 1; i++){
				sumvalue.set(sumvalue.get(1) + price.get((int)(i)));
			}
		}else{
			sumvalue.set(sumvalue.get(1) + price.get() - price.get((int)(length)));
		}
		return sumvalue.get();
	}

	/**
	 * 求波峰点
	 */
	public double swinghigh(double instance, DoubleSeries price, double strength, double length) {
		double pivotprice = 0.0;
		double pivotbar = 0.0;

		pivot(price, length, strength, strength, instance, 1, pivotprice, pivotbar);
		return pivotprice;
	}

	/**
	 * 求波峰点出现的bar
	 */
	public double swinghighbar(double instance, DoubleSeries price, double strength, double length) {
		double pivotprice = 0.0;
		double pivotbar = 0.0;

		pivot(price, length, strength, strength, instance, 1, pivotprice, pivotbar);
		return pivotbar;
	}

	/**
	 * 求波谷点
	 */
	public double swinglow(double instance, DoubleSeries price, double strength, double length) {
		double pivotprice = 0.0;
		double pivotbar = 0.0;

		pivot(price, length, strength, strength, instance, -1, pivotprice, pivotbar);
		return pivotprice;
	}

	/**
	 * 求波谷点出现的bar
	 */
	public double swinglowbar(double instance, DoubleSeries price, double strength, double length) {
		double pivotprice = 0.0;
		double pivotbar = 0.0;

		pivot(price, length, strength, strength, instance, -1, pivotprice, pivotbar);
		return pivotbar;
	}

	/**
	 * 返回指定bar的真正交易日期
	 */
	public double truedate(double length) {
		double newdate = 0.0;
		double dayoffset = 0.0;
		double dow = 0.0;

		if(bartype() == 0){
			newdate = date(length);
		}else{
			dayoffset = 0;
			dow = weekday(length);
			if(hour(length) >= 18){
				if(dow == friday()){
					dayoffset = 3;
				}else if(dow == saturday()){
					dayoffset = 2;
				}else{
					dayoffset = 1;
				}
			}else{
				if(dow == saturday()){
					dayoffset = 2;
				}else if(dow == sunday()){
					dayoffset = 1;
				}
			}
			newdate = dateadd(date(length), dayoffset);
		}
		return newdate;
	}

	/**
	 * 求真实高点
	 */
	public double truehigh() {
		double thighvalue = 0.0;

		thighvalue = close(1);
		if(high() >= close(1))
			thighvalue = high();
		return thighvalue;
	}

	/**
	 * 求真实低点
	 */
	public double truelow() {
		double tlowvalue = 0.0;

		tlowvalue = close(1);
		if(low() <= close(1))
			tlowvalue = low();
		return tlowvalue;
	}

	/**
	 * 求真实范围
	 */
	public double truerange() {

		if(currentbar() == 0)
			return high() - low();
		else
			return truehigh() - truelow();
	}

	/**
	 * 求估计方差。
	 */
	public double varianceps(DoubleSeries Price, double length, double dataType) {
		double Divisor;
		double SumSqr = 0;
		double Mean;

		Divisor = length - 1;
		if(dataType == 1)
			Divisor = length;
		if(Divisor > 0){
			Mean = average(Price, length);
			for(int i = 0; i < length; i++){
				SumSqr = SumSqr + sqr(Price.get(i) - Mean);
			}
			return SumSqr / Divisor;
		}else{
			return 0;
		}
	}

	/**
	 * 求n天前的成交量
	 */
	public double vold(double daysago) {
		DoubleSeries barcnt = newDoubleSeries(0.0, varsName("barcnt"));
		DoubleSeries dayvol = newDoubleSeries(0.0, varsName("dayvol"));
		double i = 0.0;
		double j = 0.0;
		double nindex = 0;
		double cbindex = 0.0;

		cbindex = currentbar();
		if(cbindex == 0 || truedate(0) != truedate(1)){
			barcnt.set(1);
			dayvol.set(vol());
		}else{
			barcnt.set(barcnt.get(1) + 1);
			dayvol.set(dayvol.get(1) + vol());
		}
		if(daysago == 0){
			return dayvol.get();
		}else{
			for(i = 1; i <= daysago; i++){
				if(i == 1){
					j = 0;
				}else{
					j = j + barcnt.get((int)(j));
				}
				if(j > cbindex)
					return invalidnumeric();
				nindex = nindex + barcnt.get((int)(j));
			}
			return dayvol.get((int)(nindex));
		}
	}

	/**
	 * 求权重平均
	 */
	public double waverage(DoubleSeries price, double length) {
		double wtdsum = 0;
		double cumwt = 0.0;
		double i = 0.0;

		for(i = 0; i <= length - 1; i++){
			wtdsum = wtdsum + (length - i) * price.get((int)(i));
		}
		cumwt = (length + 1) * length * 1 / 2;
		return wtdsum / cumwt;
	}

	/**
	 * 求指数平均
	 */
	public double xaverage(Double price, Double length) {
		double sfcactor = 0.0;
		DoubleSeries xavgvalue = newDoubleSeries(0.0, varsName("xavgvalue"));

		sfcactor = 2 / (length + 1);
		if(currentbar() == 0){
			xavgvalue.set(price);
		}else{
			xavgvalue.set(xavgvalue.get(1) + sfcactor * (price - xavgvalue.get(1)));
		}
		return xavgvalue.get();
	}
	
	/**
	 * 集合竞价与小节过滤函数
	 */
	public boolean callauctionfilter() {
		if (barstatus() == 2 && date() >= currentdate()) {
			if (exchangename() == "上海证券交易所" || exchangename() == "深圳证券交易所") {
				if (time() == 0.0900 && currenttime() > 0.092455 && currenttime() < 0.093005)
					return false;
				if (time() == 0.0930 && currenttime() < 0.093005)
					return false;
				if (time() == 0.1300 && currenttime() < 0.130005)
					return false;
			} else if (exchangename() == "中国金融期货交易所") {
				if ((bartype() == 0 || bartype() == 4 || bartype() == 5) && currenttime() > 0.091355 && currenttime() < 0.091505)
					return false;
				if (time() == 0.0900 && currenttime() > 0.091355 && currenttime() < 0.091505)
					return false;
				if (time() == 0.0915 && currenttime() < 0.091505)
					return false;
				if (time() == 0.1300 && currenttime() < 0.130005)
					return false;
			} else if (exchangename() == "上海期货交易所" || exchangename() == "郑州商品交易所" || exchangename() == "大连商品交易所") {
				if ((bartype() == 0 || bartype() == 4 || bartype() == 5) && currenttime() > 0.205855 && currenttime() < 0.210005)
					return false;
				if ((bartype() == 0 || bartype() == 4 || bartype() == 5) && currenttime() > 0.085855 && currenttime() < 0.090005)
					return false;
				if (time() == 0.2100 && currenttime() < 0.210005)
					return false;
				if (time() == 0.0900 && currenttime() < 0.090005)
					return false;
				if (time() == 0.1030 && currenttime() < 0.103005)
					return false;
				if ((time() == 0.1300 || time() == 0.1330) && currenttime() < 0.133005)
					return false;
			}
		}
		return true;
	}
}