package com.dfitc.stpc81f6606f1b3.strategy.baseStrategy;

import java.util.ArrayList;
import java.util.List;
import com.dfitc.stp.annotations.Combo;
import com.dfitc.stp.annotations.Direction;
import com.dfitc.stp.annotations.In;
import com.dfitc.stp.annotations.IntegerValidator;
import com.dfitc.stp.annotations.Item;
import com.dfitc.stp.annotations.NumberRangeValidator;
import com.dfitc.stp.annotations.Offset;
import com.dfitc.stp.annotations.Out;
import com.dfitc.stp.annotations.OutputMode;
import com.dfitc.stp.annotations.RegexValidator;
import com.dfitc.stp.annotations.RequiredStringValidator;
import com.dfitc.stp.annotations.Strategy;
import com.dfitc.stp.annotations.StringLengthValidator;
import com.dfitc.stp.annotations.Text;
import com.dfitc.stp.annotations.Validations;
import com.dfitc.stp.entity.Contract;
import com.dfitc.stp.entity.ContractType;
import com.dfitc.stp.entity.Position;
import com.dfitc.stp.market.Bar;
import com.dfitc.stp.market.BarSeries;
import com.dfitc.stp.market.MD;
import com.dfitc.stp.strategy.BaseStrategy;
import com.dfitc.stp.strategy.Prop;
import com.dfitc.stp.trader.Constants;
import com.dfitc.stp.trader.OrderCancelResult;
import com.dfitc.stp.trader.OrderDealResult;
import com.dfitc.stp.trader.OrderRelevant;
import com.dfitc.stp.util.MathUtil;

/**
 * 策略开发目的：用于大商所之外的交易所品种下单测试，有报单和撤单两种基本功能，包含未成交委托号的输出参数，撤单时从输出参数中获取委托号进行撤单操作
 */

@Strategy(name = "异步下单", version = "2.20", outputMode = OutputMode.TIMER, outputPeriod = 3000, contractNumber = 1)
public class AsynchronousOrder extends BaseStrategy {

	@In(label = "请选择操作类型", sequence = 0)
	@Combo(readonly = true, selectIndex = 0, items = {
			@Item(key = "报单", value = "10"), @Item(key = "撤单", value = "11") })
	int action;

	@In(label = "买卖方向", sequence = 1)
	@Direction(0)
	int direct;

	@In(label = "开平方向", sequence = 2)
	@Offset(0)
	int OpenCloseFlag;

	@In(label = "下单价格", sequence = 3)
	@Text(value = "0.0", readonly = false)
	double price;

	@In(label = "下单数量", sequence = 4)
	@Text(value = "1", readonly = false)
	int vol;

	@In(label = "撤单委托号", sequence = 5)
	@Text(value = "000000000000000001", readonly = false)
	String OrderNumber;

	@Out(label = "olh", sequence = 6)
	int olh;
	@Out(label = "osh", sequence = 7)
	int osh;
	@Out(label = "clh", sequence = 8)
	int clh;
	@Out(label = "csh", sequence = 9)
	int csh;

	OrderRelevant order;
	double tick;
	int x = 0;

	/**
	 	
	 */
	@Override
	public void setBarCycles(String[] contracts) {
	}

	/**
	 	*
	 */
	@Override
	public void setIndicators(String[] contracts) {

	}

	/**
	 * 初始化方法：在策略被实例化后自动调用， 在此可以初始化local parameter
	 */
	@Override
	public void initialize(String[] contracts) {
		setAutoPauseBySystem(false);
		setAutoResumeBySystem(false);
		//loadPosition(getBindAccountID(0));
		tick = this.getMinMove();
		System.out.println("最小变动价位：" + tick);
		
	}

	@Override
	public void processBar(Bar bar, BarSeries barSeries) {
	}

	@Override
	public void postStartStrategy() {

		x = 0;
		if (action == 10) {
			order = this.orderInsert(MathUtil.lower(price + tick / 2, tick),
					vol, direct, OpenCloseFlag);
		} else if (action == 11) {
			orderCancelSync(this.getOrderRelevant(OrderNumber));
		}

	}

	@Override
	public void postResumeStrategy() {

		x = 0;
		if (action == 10) {
			order = this.orderInsert(MathUtil.lower(price + tick / 2, tick),
					vol, direct, OpenCloseFlag);
		} else if (action == 11) {
			this.orderCancelSync(this.getOrderRelevant(OrderNumber));
		}

	}

	@Override
	public void processMD(MD md) {

		x++;
		olh = getHangingVol(getContractCode(), 1, 1);
		osh = getHangingVol(getContractCode(), -1, 1);
		csh = getHangingVol(getContractCode(), 1, -1);
		clh = getHangingVol(getContractCode(), -1, -1);

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
