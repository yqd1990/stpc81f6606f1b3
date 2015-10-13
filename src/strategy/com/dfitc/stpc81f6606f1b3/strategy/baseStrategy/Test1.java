package com.dfitc.stpc81f6606f1b3.strategy.baseStrategy;

import com.dfitc.stp.annotations.*;
import com.dfitc.stp.indicator.*;
import com.dfitc.stp.market.*;
import com.dfitc.stp.trader.*;
import com.dfitc.stp.strategy.*;
import com.dfitc.stp.entity.Contract;
import com.dfitc.stp.entity.Time;
import com.dfitc.stp.util.MathUtil;
import com.dfitc.stp.util.StringUtil;
import java.util.Date;

import com.dfitc.stpc81f6606f1b3.indicator.baseIndicator.MA;


/**
 * 策略描述:
 */
	
	
@Strategy(name = "Test1",version="1.0",outputMode = OutputMode.TIMER, outputPeriod = 3000, contractNumber = 1)
public class Test1 extends BaseStrategy {
	/**
	 * 参数描述:
	 */

	@In(label = "计算MA所需K线数", sequence = 0)
	@Text(value = "5", readonly = false)
	int len;
	
	/**
	 * 参数描述:
	 */

	@In(label = "周期", sequence = 1)
	@Cyc("1m")
	Cycle cyc;
	
	

	@In(label = "参数3", sequence = 2)
	@DateTime(value = "13:53:13", format = "HH:mm:ss", style = Style.TIME)
	Time p3;
	
	

	@Out(label = "最新价", sequence = 3)
	double lastPrice;
	
	//@Out
	MA ma;
	int x;

	
	
	@Event(name = "买开", sequence = 0, argDisplay = false, arg = "")
	public Object buyOpen(String arg ) {
		// buyToOpen(1);
		//this.orderInsert(3300, 1, 1, 1);
		return "买开";
	}	
	
	@Event(name = "卖开", sequence = 1, argDisplay = true, arg = "1")
	public Object sellOpen(String arg ) {
		int sellVol = Integer.parseInt(arg);
		//sellToOpen(sellVol);
		return "卖开";
	}	
	
 	/**
	 
 * 初始化K线周期，在策略创建时被调用(在initialize之后调用)	
 * @param contracts策略相关联的合约
	 */
	@Override
	public void setBarCycles(String[] contracts) {
		 importBarCycle(contracts[0], cyc);
	}

	/**
	 
 * 初始化指标，在策略创建时被调用(在initialize之后调用)	
 * @param contracts策略相关联的合约
	 */
	@Override
	public void setIndicators(String[] contracts) {
		ma = (MA)this.importIndicator(new MA(len), contracts[0], cyc);
		this.ma.setIndicatorNames(new String[] { "ma" });
		
	}
	
	/**
	 * 初始化方法，在策略创建时调用
	 * 
	 * @param contracts
	 *            策略关联的合约
	 */
	@Override
	public void initialize(String[] contracts) {
		this.setAutoPauseBySystem(false);
		this.setAutoResumeBySystem(false);
		this.setAutoPauseByLimit(false);
	}
	/**
	 * 处理K线
	 * 
	 * @param bar
	 *            触发此次调用的K线
	 * @param barSeries
	 *            此次K线所对应的K线序列(barSeries.get()与bar是等价的)
	 */
	public void processBar(Bar bar, BarSeries barSeries) {

		double avg = MathUtil.avg(barSeries.getSubCloseSeries(len));
		// plotNum("close", avg);
		plotNum("close", bar.getClose(), Locator.NONE, 0);
		// plotString("close", ""+bar.getIndex());
		
		// Contract c = getContractObject(getContractCode());
		// c.getContractSizeValue();
		// getDirPositionObject().getDirPositionProfitMoney(1);

	}
	
	
	@Override
	public void processMD(MD md) {
		lastPrice = md.getLatestPrice();
	}

	
}
