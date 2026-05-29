package com.bmgf.service.impl;
import com.bmgf.dao.HonoraryTitleRepository;
import com.bmgf.po.HonoraryTitle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class HonoraryTitleService {
    @Autowired
    private HonoraryTitleRepository honoraryTitleRepository;
    public List<HonoraryTitle> findAll() {
        return honoraryTitleRepository.findAll();
    }
    public Boolean insert(HonoraryTitle honoraryTitle){
        if (honoraryTitle == null) {
            return false;
        }else {
            honoraryTitleRepository.save(honoraryTitle);
            return true;
        }
    }
}
