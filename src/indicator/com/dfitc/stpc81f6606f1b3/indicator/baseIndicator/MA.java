package com.dfitc.stpc81f6606f1b3.indicator.baseIndicator;

import com.dfitc.stp.indicator.BarIndicator;
import com.dfitc.stp.market.Bar;
import com.dfitc.stp.market.BarSeries;
import com.dfitc.stp.util.DoubleSeries;
import com.dfitc.stp.util.IndicatorUtil;

/**
 * 指标类型:指标样例. 指标描述: MA指标<br>
 * 公式参考:MA = SUM(Price, N)/N<br>
 * 其中，Price表示计算MA时所使用的价格，可以是收盘价、开盘价、最高价、最低价，这个参数在构造MA时提供，默认使用收盘价计算MA
 */
public class MA extends BarIndicator {
	private int n;
	private DoubleSeries x;

	public MA() {

	}

	/**
	 * 构造函数(创建一个使用收盘价来计算的MA指标)
	 * 
	 * @param n
	 *            MA的默认取值长度
	 */
	public MA(int n) {
		super(n);
		this.n = n;
		this.x = new DoubleSeries(n + 1);// 注意：为了在第1次计算MA之后能快速计算MA，需要数组的长度为n+1
	}

	@Override
	public boolean preCalculateBar(Bar bar, BarSeries barSeries,
			double[] results) {
		//System.out.println("preCalculateBar:"+barSeries.size());
		addOrSetToSeries(x, barSeries.close(), bar);
		
		return x.size() >= n;// 此处不能用isFull
	}

	@Override
	public void calculateBar(Bar bar, BarSeries barSeries, double[] results) {
		//System.out.println("calculateBar:"+barSeries.size());
		if (isCalculated()) {
			// 如果不是第1次计算则采用快速算法：(lastMA*N-X[N]+X[0])/N
			results[0] = IndicatorUtil.ma(x, n, results[0]);
		} else {
			// 第1次计算
			results[0] = IndicatorUtil.ma(x, n);
		}
	}

	/**
	 * 将特定值添加到序列中<br>
	 * 
	 * 若该指标为实时计算指标（即每来一笔行情调用一次，K线未计算完成时也会调用），则在每根K线第一笔行情向序列中添加数据，之后每次调用仅修改该数据<br>
	 * 若该指标为非实时计算指标（只在每根k线计算完成时调用一次），则每次调用都向序列中添加数据
	 */
	public void addOrSetToSeries(DoubleSeries ds, double value, Bar bar) {
		
		if (bar.isBeginning() || !isInside()) {
			ds.add(value);
		} else {
			ds.set(value);
		}
	
	}
}
