package com.dfitc.stpc81f6606f1b3.strategy.baseStrategy;

import com.dfitc.stp.annotations.Cyc;
import com.dfitc.stp.annotations.Event;
import com.dfitc.stp.annotations.In;
import com.dfitc.stp.annotations.Out;
import com.dfitc.stp.annotations.OutputMode;
import com.dfitc.stp.annotations.Strategy;
import com.dfitc.stp.annotations.Text;
import com.dfitc.stp.market.Bar;
import com.dfitc.stp.market.BarSeries;
import com.dfitc.stp.market.Cycle;
import com.dfitc.stp.market.MD;
import com.dfitc.stp.market.XCubeIndicator;
import com.dfitc.stp.strategy.BaseStrategy;
import com.dfitc.stp.strategy.Locator;
import com.dfitc.stp.strategy.Lock;
import com.dfitc.stp.trader.Constants;
import com.dfitc.stp.trader.OrderCancelResult;
import com.dfitc.stp.trader.OrderDealResult;
import com.dfitc.stp.trader.OrderRelevant;
import com.dfitc.stp.trader.OrderStatusResult;
import com.dfitc.stp.util.IndicatorUtil;
import com.dfitc.stpc81f6606f1b3.indicator.baseIndicator.MA;

/**
 * 策略描述：MA均线穿越演示策略
 * 当快线MA上穿慢线MA时买进(若有空头持仓则平仓)，当快线MA下穿慢线MA时卖出，卖出后反手开仓
 * 策略仅用于演示
 */

@Strategy(name = "MA穿越演示策略", version = "1.0", outputMode = OutputMode.TIMER, outputPeriod = 3000, contractNumber = 1)
public class MACross extends BaseStrategy {
	/**
	 * 参数描述:
	 */
	@In(label = "快线长度", sequence = 0)
	@Text(value = "7", readonly = false)
	int fastLen = 7;

	/**
	 * 参数描述:
	 */
	@In(label = "慢线长度", sequence = 1)
	@Text(value = "20", readonly = false)
	int slowLen = 20;

	/**
	 * 参数描述:
	 */
	@In(label = "策略周期", sequence = 2)
	@Cyc("5m")
	Cycle cyc = Cycle.valueOf("5m");
	
	
	@In(label = "手数", sequence = 3, each = true)
	@Text(value = "1", readonly = false)
	int vol;

	@Out
	MA fast_ma;
	@Out
	MA slow_ma;

	
	
	
	/**
	 * 
	 * 初始化K线周期，在策略创建时被调用(在initialize之后调用)
	 * 
	 * @param contracts策略相关联的合约
	 */
	@Override
	public void setBarCycles(String[] contracts) {
		importBarCycle(contracts[0], cyc);
	}

	/**
	 * 
	 * 初始化指标，在策略创建时被调用(在initialize之后调用)
	 * 
	 * @param contracts策略相关联的合约
	 */
	@Override
	public void setIndicators(String[] contracts) {
		fast_ma = (MA) this.importIndicator(new MA(fastLen), contracts[0], cyc);
		this.fast_ma.setIndicatorNames(new String[] { "fast_ma" });
		slow_ma = (MA) this.importIndicator(new MA(slowLen), contracts[0], cyc);
		this.slow_ma.setIndicatorNames(new String[] { "slow_ma" });
		importXCubeIndicator(XCubeIndicator.QUOTE.getName());
		importXCubeIndicator(XCubeIndicator.OTV.getName());
	}

	/**
	 * 初始化方法，在策略创建时调用
	 * 
	 * @param contracts
	 *            策略关联的合约
	 */
	@Override
	public void initialize(String[] contracts) {
//		loadLocalPosition();				    //加载本策略的本地文件持仓
//		loadLocalPosition("MA穿越演示策略.pos"); //加载“MA穿越演示策略”的本地文件持仓
//		loadPosition(getBindAccountID(0));      //加载策略所选第一个资金帐户的账户持仓
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
		if(barSeries.size()==2){
			//int vol1 = (Integer) getParamByContract(getContractCode(), "vol");
			//int vol2 = (Integer) getParamByContract(getContractCode(1), "vol");
			
			//System.out.println(vol1+"");
		}
		
	}

	/**
	 * 处理K线(每来一个快照行情调用一次，即K线未计算完成时也会调用，适用于bar内成交)
	 * 
	 * @param bar
	 *            触发此次调用的K线
	 * @param barSeries
	 *            此次K线所对应的K线序列(barSeries.get()与bar是等价的)
	 */
	@Override
	public void processBarInside(Bar bar, BarSeries barSeries) {
		Lock lock_long = getDirLock(getContractCode(), 1);
		if (!lock_long.isLocked()) {
			lock_long.lock(1);
		}

		Lock lock_short = getDirLock(getContractCode(), -1);
		if (!lock_short.isLocked()) {
			lock_short.lock(1);
		}

		if (IndicatorUtil.crossUp(fast_ma.getSubIndicatorSeries(),
				slow_ma.getSubIndicatorSeries())) {
			//System.out.println("做多:"+getLastMD().getTimeStamp());
			plotString("action", "做多");
			//plotString("action", "做多", Locator.CLOSE, 0);
			buyToClose(getDirPositionVol(Constants.SHORT));
			buyToOpen(1);
		} else if (IndicatorUtil.crossDown(fast_ma.getSubIndicatorSeries(),
				slow_ma.getSubIndicatorSeries())) {// 满足条件，平掉全部持仓
			//System.out.println("做空:"+getLastMD().getTimeStamp());
			plotString("action", "做空");
			//plotString("action", "做空", Locator.CLOSE, 0);
			sellToClose(getDirPositionVol(Constants.LONG));
			sellToOpen(1);
		}
	/*	if(bar.isFinished() && barSeries.size()==5){
			System.out.println(getLastMD());
		}*/
		
	}

	/**
	 * 处理TICK行情
	 * 
	 * @param md
	 *            触发此次调用的行情快照
	 */
	@Override
	public void processMD(MD md) {
	}

	/**
	 * 处理委托回报
	 * 
	 * @param result
	 *            触发此次调用的回报
	 * @param order
	 *            此次回报对应的报单
	 * @param md
	 *            此回报对应合约的最新Tick行情快照
	 */
	@Override
	public void processOrderStatus(OrderStatusResult result,
			OrderRelevant order, MD md) {

	}

	/**
	 * 处理成交回报
	 * 
	 * @param result
	 *            触发此次调用的回报
	 * @param order
	 *            此次回报对应的报单
	 * @param md
	 *            此回报对应合约的最新Tick行情快照
	 */
	@Override
	public void processOrderDeal(OrderDealResult result, OrderRelevant order,
			MD md) {
		
	}

	/**
	 * 处理撤单回报
	 * 
	 * @param result
	 *            触发此次调用的回报
	 * @param order
	 *            此次回报对应的报单
	 * @param md
	 *            此回报对应合约的最新Tick行情快照
	 */
	@Override
	public void processOrderCancel(OrderCancelResult result,
			OrderRelevant order, MD md) {
	}
}
