package com.dfitc.stpc81f6606f1b3.strategy.baseStrategy;

import com.dfitc.stp.annotations.*;
import com.dfitc.stp.indicator.*;
import com.dfitc.stp.market.*;
import com.dfitc.stp.trader.*;
import com.dfitc.stp.strategy.*;
import com.dfitc.stp.entity.Contract;
import com.dfitc.stp.entity.ContractType;
import com.dfitc.stp.entity.Position;
import com.dfitc.stp.entity.Time;
import com.dfitc.stp.util.MathUtil;
import com.dfitc.stp.util.StringUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 策略描述: 策略开发目的：大商所品种下单测试专用，应用服务器221
 */
@Strategy(name = "下单策略__股票", version = "1.0", outputMode = OutputMode.TIMER, outputPeriod = 3000, contractNumber = 1)
public class PlaceOrder_Securities extends BaseStrategy {
	@In(label = "请选择操作类型", sequence = 0)
	@Combo(readonly = true, selectIndex = 0, items = {
			@Item(key = "报单", value = "10"), @Item(key = "撤单", value = "11") })
	int action;

	@In(label = "买卖方向", sequence = 1)
	@Direction(1)
	int dir;

	@In(label = "开平方向", sequence = 2)
	@Offset(1)
	int openCloseFlag;

	@In(label = "投保类型", sequence = 3)
	@Combo(readonly = true, selectIndex = 0, items = {
			@Item(key = "投机", value = "1"), @Item(key = "套利", value = "2"),
			@Item(key = "套保", value = "3") })
	String insure_Type;

	@In(label = "定单类型", sequence = 4)
	@Combo(readonly = true, selectIndex = 0, items = {
			@Item(key = "限价委托", value = "1"),
			@Item(key = "最优五档立即成交剩余撤单（上海）", value = "11"),
			@Item(key = "最优五档立即成交剩余转限价（上海）", value = "12"),
			@Item(key = "对方最优价格（深圳）", value = "101"),
			@Item(key = "本方最优价格（深圳）", value = "102"),
			@Item(key = "即时成交剩余撤单（深圳）", value = "103"),
			@Item(key = "最优五档即时成交剩余撤单（深圳）", value = "104"),
			@Item(key = "全额成交或撤单（深圳）", value = "105") })
	String order_Type;

	@In(label = "定单属性", sequence = 5)
	@Combo(readonly = true, selectIndex = 0, items = {
			@Item(key = "缺省", value = "0"), @Item(key = "FOK", value = "1"),
			@Item(key = "FAK", value = "2") })
	String order_Attribute;

	@In(label = "止损价和止盈价", sequence = 6)
	@Text(value = "0.0", readonly = false)
	double stop_Price;
	@In(label = "申报标志", sequence = 7)
	@Combo(readonly = true, selectIndex = 0, items = {
			@Item(key = "非自动单", value = "1"), @Item(key = "自动单", value = "2") })
	String declare_Flag;

	@In(label = "下单价格", sequence = 8)
	@Text(value = "0.0", readonly = false)
	double price;

	@In(label = "下单数量", sequence = 9)
	@Text(value = "1", readonly = false)
	int vol;

	@In(label = "撤单委托号", sequence = 10)
	@Text(value = "000000000000000000", readonly = false)
	String OrderNumber;

	@In(label = "加载账户持仓", sequence = 11)
	@Combo(readonly = true, selectIndex = 0, items = {
			@Item(key = "不加载", value = "0"), @Item(key = "加载股票", value = "1"),
			@Item(key = "加载股票期权", value = "2"),
			@Item(key = "根据合约代码加载", value = "3") })
	String load;

	@In(label = "合约代码", sequence = 12)
	@Text(value = "IF", readonly = false)
	String str;

	OrderRelevant order;
	double tick;
	int x;

	/**
	 * 合约号：contracts[0] 周期：TICK 方法描述：
	 */
	@Override
	public void setBarCycles(String[] contracts) {
	}

	/**
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
	 * @param contracts策略关联的合约
	 */
	@Override
	public void initialize(String[] contracts) {
		tick = this.getMinMove();
		loadPosition(getBindAccountID(0));
	}

	@Override
	public boolean acceptPosition(Position pos) {
		Contract con = pos.getContract();
		if (load.equals("0")) {
			return false;
		}
		if (load.equals("1")) {
			if (con.getContractType() == ContractType.STOCK) {
				return true;
			}
		}
		if (load.equals("2")) {
			if (con.getContractType() == ContractType.STOCKOPTIONS) {
				return true;
			}
		}
		if (load.equals("3")) {
			if (con.getContractType().equals(str)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void postStartStrategy() {
		x = 0;
		if (action == 10) {
			double price2 = MathUtil.lower(price + tick / 2, tick);
			System.out.println("价格：" + price);
			System.out.println("tick:" + tick);
			System.out.println("规整后的价格" + price2);
			order = orderInsert(getContractCode(), price2, vol, dir,
					openCloseFlag, insure_Type, order_Type, order_Attribute,
					stop_Price, declare_Flag);
			System.out.println("委托价：" + order.getInputPrice());
		} else if (action == 11) {
			this.orderCancelSync(this.getOrderRelevant(OrderNumber));
		}
	}

	@Override
	public void postResumeStrategy() {
		x = 0;
		if (action == 10) {
			order = orderInsert(getContractCode(),
					MathUtil.lower(price + tick / 2, tick), vol, dir,
					openCloseFlag, insure_Type, order_Type, order_Attribute,
					stop_Price, declare_Flag);
		} else if (action == 11) {
			this.orderCancelSync(this.getOrderRelevant(OrderNumber));
		}
	}

	/**
	 * 处理K线
	 * 
	 * @param k触发此次调用的K线
	 * @param kseries此次K线所对应的K线序列
	 *            (kseries.get()与k是等价的)
	 */
	public void processBar(Bar bar, BarSeries barSeries) {
	}

	/**
	 * 处理TICK行情
	 * 
	 * @param market触发此次调用的行情快照
	 */
	@Override
	public void processMD(MD md) {
		x++;
		if (x == 1) {
			System.out.println("涨停：" + md.getUpperLimitPrice());
			System.out.println("跌停：" + md.getLowerLimitPrice());
		}
	}

	/**
	 * 处理委托回报
	 * 
	 * @param answer触发此次调用的回报
	 * @param order此次回报对应的报单
	 * @param market此回报对应合约的最新Tick行情快照
	 */
	@Override
	public void processOrderStatus(OrderStatusResult result,
			OrderRelevant order, MD md) {
		//System.out.println(order.getInputPrice());
	}

	/**
	 * 处理成交回报
	 * 
	 * @param answer触发此次调用的回报
	 * @param order此次回报对应的报单
	 * @param market此回报对应合约的最新Tick行情快照
	 */
	@Override
	public void processOrderDeal(OrderDealResult result, OrderRelevant order,
			MD md) {
	}

	/**
	 * 处理撤单回报
	 * 
	 * @param answer触发此次调用的回报
	 * @param order此次回报对应的报单
	 * @param market此回报对应合约的最新Tick行情快照
	 */
	@Override
	public void processOrderCancel(OrderCancelResult result,
			OrderRelevant order, MD md) {
	}
}
