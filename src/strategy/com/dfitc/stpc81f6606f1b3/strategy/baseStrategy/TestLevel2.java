package com.dfitc.stpc81f6606f1b3.strategy.baseStrategy;

import com.dfitc.stp.annotations.*;
import com.dfitc.stp.indicator.*;
import com.dfitc.stp.market.*;
import com.dfitc.stp.trader.*;
import com.dfitc.stp.strategy.*;
import com.dfitc.stp.entity.Time;
import com.dfitc.stp.util.StringUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 策略描述:
 */

@Strategy(name = "TestLevel2", version = "1.0", outputMode = OutputMode.TIMER, outputPeriod = 3000, contractNumber = 1)
public class TestLevel2 extends BaseStrategy {

	/**
	 * 
	 * 初始化K线周期，在策略创建时被调用(在initialize之后调用)
	 * 
	 * @param contracts策略相关联的合约
	 */
	@Override
	public void setBarCycles(String[] contracts) {
	}

	/**
	 * 
	 * 初始化指标，在策略创建时被调用(在initialize之后调用)
	 * 
	 * @param contracts策略相关联的合约
	 */
	@Override
	public void setIndicators(String[] contracts) {

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
	}

	@Override
	public void processMD(MD md) {
//		pbuy(md);
//		psell(md);
		if(md.getTimeStamp().after(getD("20150407 14:05:00")) && md.getTimeStamp().before(getD("20150407 14:07:00"))){
			log.appendFile("leve2.txt", md);
		}
		
	}
	public Date getD(String dateStr){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		Date d = null;
		try {
			d = sdf.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d;
	}

	public void pbuy(MD md) {
		System.out.print(md.getTimeStamp()+"  ");
		DepthMDItem[] buys = md.getL2MDBidArray();
		System.out.print("五档买：");
		for (int i = 0; i < buys.length; i++) {
			System.out.print(buys[i]);
		}
		System.out.print("   ");
		
		DepthMDItem[] sells = md.getL2MDAskArray();
		System.out.print("五档卖：");
		for (int i = 0; i < sells.length; i++) {
			System.out.print(sells[i]);
		}
		System.out.println();
	}

	public void psell(MD md) {
		
	}

}
