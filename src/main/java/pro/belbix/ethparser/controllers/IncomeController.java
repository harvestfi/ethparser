package pro.belbix.ethparser.controllers;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import pro.belbix.ethparser.entity.IncomeEntity;
import pro.belbix.ethparser.repositories.IncomeRepository;

@ConditionalOnExpression("!${ethparser.onlyParse:false}")
@RestController
public class IncomeController {

    private final IncomeRepository repository;

    public IncomeController(IncomeRepository repository) {
        this.repository = repository;
    }

    @Deprecated
    @RequestMapping(value = "api/transactions/last/income", method = RequestMethod.GET)
    public IncomeEntity lastIncome() {
        return repository.findFirstByOrderByTimestampDesc();
    }

    @Deprecated
    @RequestMapping(value = "api/transactions/history/income", method = RequestMethod.GET)
    public Iterable<IncomeEntity> incomeHistory() {
        return repository.findAllByOrderByTimestamp();
    }

}
