package com.dfitc.stpc81f6606f1b3.strategy.baseStrategy;

import com.dfitc.stp.annotations.*;
import com.dfitc.stp.indicator.*;
import com.dfitc.stp.market.*;
import com.dfitc.stp.trader.*;
import com.dfitc.stp.strategy.*;
import com.dfitc.stp.entity.Time;
import com.dfitc.stp.util.MathUtil;
import com.dfitc.stp.util.StringUtil;
import java.util.Date;

/** 
 * 策略描述:
 */
@Strategy(name = "交易所套利异步报单2", version = "1.0", outputMode = OutputMode.TIMER, outputPeriod = 3000, contractNumber = 1)
public class ExchangeArbitrageAsyOrder2 extends BaseStrategy {
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
	@In(label = "是否加载持仓", sequence = 5)
	@Bool(true)
	boolean isloadP;

	@Event(name = "获取多仓", sequence = 0, argDisplay = false, arg = "", desc = "qqqqq")
	public Object getLong(String arg) {
		System.out.println("获取多头持仓：" + getDirPositionVol(getContractCode(), 1));
		return "获取多仓";
	}

	@Event(name = "获取空仓", sequence = 1, argDisplay = false, arg = "")
	public Object getShort(String arg) {
		System.out
				.println("获取空头持仓：" + getDirPositionVol(getContractCode(), -1));
		return "获取多仓";
	}

	@Event(name = "获取多头挂单", sequence = 2, argDisplay = false, arg = "")
	public Object getLongHang(String arg) {
		int openHang = getHangingVol(getContractCode(), 1, 1);
		int closeHang = getHangingVol(getContractCode(), 1, -1);
		System.out.println("获取多头挂单（开，平）：" + openHang + "," + closeHang);
		return "获取多头挂单";
	}

	@Event(name = "获取空头挂单", sequence = 3, argDisplay = false, arg = "")
	public Object getShortHang(String arg) {
		int openHang = getHangingVol(getContractCode(), -1, 1);
		int closeHang = getHangingVol(getContractCode(), -1, -1);
		System.out.println("获取空头挂单（开，平）：" + openHang + "," + closeHang);
		return "获取空头挂单";
	}

	@Event(name = "持仓盈亏", sequence = 4, argDisplay = false, arg = "")
	public Object buyP(String arg) {
		double lp = getDirPositionProfit(1);
		double sp = getDirPositionProfit(-1);
		System.out.println("持仓盈亏(多，空)：" + lp + "," + sp);
		return "持仓盈亏";
	}

	@Event(name = "平仓盈亏", sequence = 5, argDisplay = false, arg = "")
	public Object sellP(String arg) {
		double lcp = getDirExitProfit(1);
		double scp = getDirExitProfit(-1);
		System.out.println("平仓盈亏(多，空)：" + lcp + "," + scp);
		return "平仓盈亏";
	}

	@Event(name = "买开", sequence = 6, argDisplay = false, arg = "")
	public Object buyopen(String arg) {
		this.orderInsert(price, 1, 1, 1);
		return "买开";
	}

	@Event(name = "卖开", sequence = 7, argDisplay = false, arg = "")
	public Object sellopen(String arg) {
		this.orderInsert(price, 1, -1, 1);
		return "卖开";
	}

	@Event(name = "买平", sequence = 8, argDisplay = false, arg = "")
	public Object buyclose(String arg) {
		this.orderInsert(price, 1, 1, -1);
		return "买平";
	}

	@Event(name = "卖平", sequence = 9, argDisplay = false, arg = "")
	public Object sellclose(String arg) {
		this.orderInsert(price, 1, -1, -1);
		return "卖平";
	}

	
	@Event(name = "启动", sequence =10, argDisplay = false, arg = "")
	public Object started(String arg) {
		registerTask("task_1", 3, Unit.SECOND, false);
		return "启动";
	}
	
	@Event(name = "暂停|继续", sequence =11, argDisplay = false, arg = "")
	public Object changeFlag(String arg) {
		if(canRun){
			canRun = false;
			return "暂停";
		}else{
			canRun = true;
			return "继续";
		}
	}
	
	@Event(name = "持仓均价", sequence = 12, argDisplay = false, arg = "")
	public Object getAvgP(String arg) {
		double buyAvg = getDirPositionAvgPrice(1);
		double sellAvg = getDirPositionAvgPrice(-1);
		System.out.println("持仓均价(买，卖)："+buyAvg+","+sellAvg);
		return "持仓均价";
	}
	@Event(name = "持仓均价2", sequence = 13, argDisplay = false, arg = "")
	public Object getAvgP2(String arg) {
		double buyAvg = getDirPositionAvgPrice(getContractCode(1), 1);
		double sellAvg = getDirPositionAvgPrice(getContractCode(1), -1);
		System.out.println("持仓均价2(买，卖)："+buyAvg+","+sellAvg);
		return "持仓均价2";
	}

	OrderRelevant order;
	double tick;
	int x = 0;
	String con1;
	String con2;
	boolean canRun = true;

	
	@Override
	public void executeTask(String taskId) {
		if(canRun){
			double lpv = getDirPositionVol(1);
			double spv = getDirPositionVol(-1);
			int lh = getHangingVol(getContractCode(), 1, 1);
			int sh = getHangingVol(getContractCode(), -1, 1);
			if(lpv == 1 && spv == 1){
				this.orderInsert(price-1, 1, -1, -1);
				this.orderInsert(price+1, 1, 1, -1);
			}else if(lpv == 0 && spv == 0 && lh == 0 && sh == 0){
				this.orderInsert(price+1, 1, 1, 1);
				this.orderInsert(price-1, 1, -1, 1);
			}
		}
		
	}
	
	
	@Override
	public void setBarCycles(String[] contracts) {
	}

	/** 
	 * 初始化指标，在策略创建时被调用(在initialize之后调用)
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
		if (isloadP) {
			loadLocalPosition();
		}
	}

	/** 
	 * 处理K线
	 * @param bar触发此次调用的K线
	 * @param barSeries此次K线所对应的K线序列(barSeries.get()与bar是等价的)
	 */
	public void processBar(Bar bar, BarSeries barSeries) {
	}

	@Override
	public void processMD(MD md) {
		x++;
		if (x == 1) {
			Capital aa = queryCapital(getBindAccountID(0));
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
