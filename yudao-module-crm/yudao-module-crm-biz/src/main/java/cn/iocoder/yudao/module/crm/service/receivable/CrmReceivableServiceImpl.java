package cn.iocoder.yudao.module.crm.service.receivable;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.CrmReceivableCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.CrmReceivableExportReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.CrmReceivablePageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.CrmReceivableUpdateReqVO;
import cn.iocoder.yudao.module.crm.convert.receivable.CrmReceivableConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.CrmContractDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.CrmReceivableDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.CrmReceivablePlanDO;
import cn.iocoder.yudao.module.crm.dal.mysql.receivable.CrmReceivableMapper;
import cn.iocoder.yudao.module.crm.enums.common.CrmAuditStatusEnum;
import cn.iocoder.yudao.module.crm.service.contract.CrmContractService;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;

/**
 * 回款管理 Service 实现类
 *
 * @author 赤焰
 */
@Service
@Validated
public class CrmReceivableServiceImpl implements CrmReceivableService {

    // TODO @liuhongfeng：crm 前缀，变量可以不带哈；
    @Resource
    private CrmReceivableMapper crmReceivableMapper;
    @Resource
    private CrmContractService contractService;
    @Resource
    private CrmCustomerService crmCustomerService;
    @Resource
    private CrmReceivablePlanService crmReceivablePlanService;

    // TODO @liuhongfeng：创建还款后，是不是什么时候，要更新 plan？
    @Override
    public Long createReceivable(CrmReceivableCreateReqVO createReqVO) {
        // 插入
        CrmReceivableDO receivable = CrmReceivableConvert.INSTANCE.convert(createReqVO);
        // TODO @liuhongfeng：这里的括号要注意排版；
        if (ObjectUtil.isNull(receivable.getStatus())){
            receivable.setStatus(CommonStatusEnum.ENABLE.getStatus());
        }
        if (ObjectUtil.isNull(receivable.getCheckStatus())){
            receivable.setCheckStatus(CrmAuditStatusEnum.DRAFT.getStatus());
        }

        // TODO @liuhongfeng：一般来说，逻辑的写法，是要先检查，后操作 db；所以，你这个 check 应该放到  CrmReceivableDO receivable 之前；
        // 校验
        checkReceivable(receivable);

        crmReceivableMapper.insert(receivable);
        return receivable.getId();
    }

    // TODO @liuhongfeng：这里的括号要注意排版；
    private void checkReceivable(CrmReceivableDO receivable) {
        // TODO @liuhongfeng：这个放在参数校验合适
        if(ObjectUtil.isNull(receivable.getContractId())){
            throw exception(CONTRACT_NOT_EXISTS);
        }

        CrmContractDO contract = contractService.getContract(receivable.getContractId());
        if(ObjectUtil.isNull(contract)){
            throw exception(CONTRACT_NOT_EXISTS);
        }

        CrmCustomerDO customer = crmCustomerService.getCustomer(receivable.getCustomerId());
        if(ObjectUtil.isNull(customer)){
            throw exception(CUSTOMER_NOT_EXISTS);
        }

        CrmReceivablePlanDO receivablePlan = crmReceivablePlanService.getReceivablePlan(receivable.getPlanId());
        if(ObjectUtil.isNull(receivablePlan)){
            throw exception(RECEIVABLE_PLAN_NOT_EXISTS);
        }

    }

    @Override
    public void updateReceivable(CrmReceivableUpdateReqVO updateReqVO) {
        // 校验存在
        validateReceivableExists(updateReqVO.getId());

        // 更新
        CrmReceivableDO updateObj = CrmReceivableConvert.INSTANCE.convert(updateReqVO);
        crmReceivableMapper.updateById(updateObj);
    }

    @Override
    public void deleteReceivable(Long id) {
        // 校验存在
        validateReceivableExists(id);
        // 删除
        crmReceivableMapper.deleteById(id);
    }

    private void validateReceivableExists(Long id) {
        if (crmReceivableMapper.selectById(id) == null) {
            throw exception(RECEIVABLE_NOT_EXISTS);
        }
    }

    @Override
    public CrmReceivableDO getReceivable(Long id) {
        return crmReceivableMapper.selectById(id);
    }

    @Override
    public List<CrmReceivableDO> getReceivableList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return crmReceivableMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<CrmReceivableDO> getReceivablePage(CrmReceivablePageReqVO pageReqVO) {
        return crmReceivableMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CrmReceivableDO> getReceivableList(CrmReceivableExportReqVO exportReqVO) {
        return crmReceivableMapper.selectList(exportReqVO);
    }

}
