CREATE OR REPLACE PROCEDURE sp_MuonSach(
    p_MaPhieu VARCHAR(20),
    p_MaBanDoc VARCHAR(20),
    p_MaBanSao VARCHAR(20)
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_TrangThai VARCHAR(50);
BEGIN
    -- Khóa bản ghi (Pessimistic Locking) bằng FOR UPDATE
    SELECT TrangThai INTO STRICT v_TrangThai
    FROM BanSao
    WHERE MaBanSao = p_MaBanSao
    FOR UPDATE;

    -- Kiểm tra điều kiện khả dụng
    IF v_TrangThai IS DISTINCT FROM 'San sang' THEN
        RAISE EXCEPTION 'Bản sao % hiện không khả dụng để mượn (Trạng thái: %)!', p_MaBanSao, COALESCE(v_TrangThai, 'Khong xac dinh');
    END IF;

    -- Ghi nhận phiếu mượn
    INSERT INTO PhieuMuon(MaPhieu, MaBanDoc, MaBanSao, NgayMuon, NgayHanTra, TrangThai)
    VALUES (p_MaPhieu, p_MaBanDoc, p_MaBanSao, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '14 days', 'Dang muon');

    -- Cập nhật trạng thái bản sao
    UPDATE BanSao SET TrangThai = 'Dang muon' WHERE MaBanSao = p_MaBanSao;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE EXCEPTION 'Không tìm thấy mã bản sao %!', p_MaBanSao;
END;
$$;