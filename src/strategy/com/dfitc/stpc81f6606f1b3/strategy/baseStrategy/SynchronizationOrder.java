package com.dfitc.stpc81f6606f1b3.strategy.baseStrategy;

import com.dfitc.stp.annotations.*;
import com.dfitc.stp.indicator.*;
import com.dfitc.stp.market.*;
import com.dfitc.stp.trader.*;
import com.dfitc.stp.strategy.*;
import com.dfitc.stp.entity.Time;
import com.dfitc.stp.util.StringUtil;
import java.util.Date;

/**
 * 策略描述:
 */

@Strategy(name = "同步报单", version = "1.0", outputMode = OutputMode.TIMER, outputPeriod = 3000, contractNumber = 1)
public class SynchronizationOrder extends BaseStrategy {

	@In(label = "报单", sequence = 0)
	@Combo(readonly = true, selectIndex = 0, items = {
			@Item(key = "买开", value = "0"), @Item(key = "买平", value = "1"),
			@Item(key = "卖开", value = "2"), @Item(key = "卖平", value = "3") })
	int action;

	@In(label = "手数", sequence = 1)
	@Text(value = "1", readonly = false)
	int vol;

	@In(label = "是否加载持仓", sequence = 2)
	@Bool(true)
	boolean isloadP;

	int x = 0;

	@Event(name = "买开", sequence = 0, argDisplay = false, arg = "")
	public Object buyopen(String arg) {
		return null;
	}

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

	@Override
	public void initialize(String[] contracts) {
		this.setAutoPauseBySystem(false);
		this.setAutoResumeBySystem(false);
		this.setAutoPauseByLimit(false);
		setCloseYesterdayPositionFlag(true);
		if(isloadP){
			loadPosition(getBindAccountID(0));
			//loadLocalPosition();
		}
		

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

		x++;
		if (x == 1) {
			if (action == 0) {
				buyToOpen(vol);
			} else if (action == 1) {
				buyToClose(vol);
			} else if (action == 2) {
				sellToOpen(vol);
			} else if (action == 3) {
				sellToClose(vol);
			}
		}
		if (x == 100) {
			System.out.println("买开挂单量："
					+ getHangingVol(getContractCode(), 1, 1));
			System.out.println("卖平挂单量："
					+ getHangingVol(getContractCode(), 1, -1));
		}

	}

	@Override
	public void postStartStrategy() {

		x = 0;

	}

	@Override
	public void postResumeStrategy() {

		x = 0;

	}

	@Override
	public void processOrderStatus(OrderStatusResult result,
			OrderRelevant order, MD md) {

	}

	@Override
	public void processOrderDeal(OrderDealResult result, OrderRelevant order,
			MD md) {

	}

	@Override
	public void processOrderCancel(OrderCancelResult result,
			OrderRelevant order, MD md) {

	}

}
